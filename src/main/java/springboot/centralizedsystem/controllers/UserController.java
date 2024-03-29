package springboot.centralizedsystem.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.Group;
import springboot.centralizedsystem.domains.ImportFile;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.GroupService;
import springboot.centralizedsystem.services.ReadSurveyService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.services.UserService;
import springboot.centralizedsystem.utils.SessionUtils;
import springboot.centralizedsystem.utils.ValidateUtils;

@Controller
public class UserController extends BaseController {

    private static final String PATH_USER = "user"; // Default resource create by form.io

    @Autowired
    private UserService userService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ReadSurveyService readSurveyService;

    @GetMapping(RequestsPath.USERS)
    public String usersGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String idGroup, @PathVariable String page, @RequestParam(required = false) String keyword,
            @ModelAttribute(Keys.IMPORT) String importMess) {
        try {
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            int currPage = Integer.parseInt(page);
            boolean isRootGroup = idGroup.equals(Configs.ROOT_GROUP);
            List<Group> listGroups = new ArrayList<>();
            List<User> listUsers = new ArrayList<>();

            long sizeListUsers = 0;
            int totalPages = 0;
            ResponseEntity<String> userResByPage = null;

            if (keyword == null) {
                // Normal case
                sizeListUsers = isRootGroup ? submissionService.countSubmissions(token, PATH_USER)
                        : userService.countUsers(token, idGroup);
                totalPages = (int) Math.ceil((float) sizeListUsers / Configs.NUMBER_ROWS_PER_PAGE);

                Group currentGroup = groupService.findGroupParent(token,
                        isRootGroup ? "data.idParent=root" : "data.idGroup=" + idGroup);
                Group parentGroup = groupService.findGroupParent(token, "data.idGroup=" + currentGroup.getIdParent());
                if (parentGroup == null) {
                    listGroups.add(currentGroup);
                } else {
                    listGroups = groupService.findListChildGroupByIdParentWithPage(token, parentGroup.getIdGroup(),
                            parentGroup.getName(), 0);
                }

                userResByPage = isRootGroup ? submissionService.findSubmissionsByPage(token, PATH_USER, currPage)
                        : userService.findUsersByPageAndIdGroup(token, idGroup, currPage);

                model.addAttribute("idGroup", isRootGroup ? Configs.ROOT_GROUP : idGroup);
            } else {
                // Search by name
                sizeListUsers = userService.countUsersByName(token, keyword);
                totalPages = (int) Math.ceil((float) sizeListUsers / Configs.NUMBER_ROWS_PER_PAGE);
                userResByPage = userService.findUsersByPageAndName(token, keyword, currPage);

                Group rootGroup = groupService.findGroupParent(token, "data.idParent=root");
                listGroups.add(rootGroup);

                model.addAttribute("idGroup", null);
                model.addAttribute("keyword", keyword);
            }

            JSONArray jsonArray = new JSONArray(userResByPage.getBody());
            JSONObject jsonObject = null;
            JSONObject dataObject = null;
            int size = jsonArray.length();
            for (int i = 0; i < size; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("_id");
                dataObject = jsonObject.getJSONObject("data");

                if (dataObject.getInt("status") == Configs.DEACTIVE_STATUS) {
                    continue;
                }

                String phoneNumber = "";
                if (!ValidateUtils.isEmptyString(dataObject, "phoneNumber")) {
                    phoneNumber = dataObject.getString("phoneNumber");
                }
                String address = "";
                if (!ValidateUtils.isEmptyString(dataObject, "address")) {
                    address = dataObject.getString("address");
                }

                String groupName = "";
                idGroup = dataObject.getString("idGroup");
                if (!idGroup.equals(Configs.ROOT_GROUP)) {
                    groupName = groupService.findGroupFiledByIdGroup(token, idGroup, "name");
                }

                listUsers.add(new User(id, dataObject.getString("email"), dataObject.getString("name"), groupName,
                        dataObject.getString("gender"), phoneNumber, address));
            }

            if (!importMess.equals("")) {
                boolean isImportSuccess = Boolean.parseBoolean(importMess);
                model.addAttribute("importMess", Messages.IMPORT(isImportSuccess));
                model.addAttribute("importStatus", isImportSuccess);
            }

            model.addAttribute("list", listUsers);
            model.addAttribute("listGroups", listGroups);
            model.addAttribute("importFile", new ImportFile());

            model.addAttribute("currPage", currPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("title", "Users management");

            return Views.USERS;
        } catch (HttpClientErrorException e) {
            return Views.ERROR_404;
        } catch (HttpServerErrorException e) {
            return Views.ERROR_500;
        }
    }

    @GetMapping(RequestsPath.CREATE_USER)
    public String createUserGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("link", APIs.modifiedForm(PATH_USER));
        model.addAttribute("token", user.getToken());
        model.addAttribute("title", "Create new User");

        return Views.SEND_REPORT;
    }

    @GetMapping(RequestsPath.EDIT_USER)
    public String editUserGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String id) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("link", APIs.modifiedForm(PATH_USER));
        model.addAttribute("token", user.getToken());
        model.addAttribute("_id", id);

        ResponseEntity<String> infoRes = userService.findUserDataById(user.getToken(), PATH_USER, id);
        model.addAttribute("data", new JSONObject(infoRes.getBody()).getJSONObject("data").toString());

        model.addAttribute("title", "Edit User");

        return Views.EDIT_REPORT;
    }

    @GetMapping(RequestsPath.PROFILE)
    public String profileGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        if (SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        String groupName = "";
        String idGroup = user.getIdGroup();
        if (!idGroup.equals(Configs.ROOT_GROUP)) {
            groupName = groupService.findGroupFiledByIdGroup(user.getToken(), idGroup, "name");
        }
        user.setNameGroup(groupName);

        int reportsNumber = user.getReportsNumber();
        int submittedNumber = user.getSubmittedNumber();
        model.addAttribute("reportsNumber", reportsNumber);
        model.addAttribute("submittedNumber", submittedNumber);
        model.addAttribute("notSubmittedNumber", reportsNumber - submittedNumber);

        model.addAttribute("user", user);

        return Views.PROFILE;
    }

    @PostMapping(RequestsPath.EDIT_PROFILE)
    public ResponseEntity<String> profilePOST(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @RequestParam("name") String name, @RequestParam("email") String email,
            @RequestParam("gender") String gender, @RequestParam("address") String address,
            @RequestParam("phone") String phoneNumber, @RequestParam("token") String token,
            @RequestParam("id") String id, @RequestParam("idGroup") String idGroup) {
        if (SessionUtils.isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user = SessionUtils.getUser(session);

        if (!id.equals(user.getId()) || !idGroup.equals(user.getIdGroup()) || !token.equals(user.getToken())) {
            return new ResponseEntity<>(Messages.UNAUTHORIZED_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        if (name.isEmpty() || name.equals("")) {
            return new ResponseEntity<>("Please fill out `name` field", HttpStatus.BAD_REQUEST);
        }
        if (email.isEmpty() || email.equals("")) {
            return new ResponseEntity<>("Please fill out `email` field", HttpStatus.BAD_REQUEST);
        }

        User newUser = new User(email, name, token, idGroup, gender, phoneNumber, address, id);

        ResponseEntity<String> res = userService.updateUserInfo(newUser, PATH_USER);

        session.setAttribute(Keys.USER, newUser);

        return res;
    }

    @PostMapping(RequestsPath.READ_USERS)
    public String readSurveyPOST(@Valid ImportFile importFile, HttpServletRequest request, HttpSession session,
            RedirectAttributes redirect) {
        redirect.addAttribute("idGroup", "root");
        redirect.addAttribute("page", 1);
        try {
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            String uploadRootPath = request.getServletContext().getRealPath("upload");

            File uploadRootDir = new File(uploadRootPath);
            // Create root upload folder if not existed.
            if (!uploadRootDir.exists()) {
                uploadRootDir.mkdirs();
            }
            MultipartFile[] fileDatas = importFile.getFileDatas();

            // Upload file to server & return path
            String path = readSurveyService.getPathFileImport(uploadRootDir, fileDatas);

            // Dump data by blue print JSON & call API insert to DB
            List<String> list = userService.getListUsersFromFile(path);
            ResponseEntity<String> res = null;
            for (String data : list) {
                res = userService.insertUser(token, data);
                if (res.getStatusCode() != HttpStatus.CREATED) {
                    redirect.addFlashAttribute(Keys.IMPORT, false);
                    return "redirect:" + RequestsPath.USERS;
                }
            }

            redirect.addFlashAttribute(Keys.IMPORT, true);
            return "redirect:" + RequestsPath.USERS;
        } catch (IOException e) {
            redirect.addFlashAttribute(Keys.IMPORT, false);
            return "redirect:" + RequestsPath.USERS;
        }
    }
}
