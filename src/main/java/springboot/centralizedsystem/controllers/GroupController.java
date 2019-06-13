package springboot.centralizedsystem.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class GroupController extends BaseController {

    private static final String PATH = "group";

    @Autowired
    private GroupService groupService;

    @Autowired
    private ReadSurveyService readSurveyService;

    @GetMapping(RequestsPath.GROUPS)
    public String groupsGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String idParent, @PathVariable String page, @ModelAttribute(Keys.IMPORT) String importMess) {
        try {
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            Group parentGroup = groupService.findGroupParent(token,
                    idParent.equals(Configs.ROOT_GROUP) ? "data.idParent=root" : "data.idGroup=" + idParent);

            int currPage = Integer.parseInt(page);
            int sizeListGroups = groupService.findNumberOfChildGroupByIdParent(token, parentGroup.getIdGroup());
            int totalPages = (int) Math.ceil((float) sizeListGroups / Configs.NUMBER_ROWS_PER_PAGE);

            List<Group> list = groupService.findListChildGroupByIdParentWithPage(token, parentGroup.getIdGroup(),
                    parentGroup.getName(), currPage);
            parentGroup.setNumberOfChildrenGroup(sizeListGroups);

            if (!importMess.equals("")) {
                boolean isImportSuccess = Boolean.parseBoolean(importMess);
                model.addAttribute("importMess", Messages.IMPORT(isImportSuccess));
                model.addAttribute("importStatus", isImportSuccess);
            }

            model.addAttribute("currPage", currPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("root", parentGroup);
            model.addAttribute("list", list);
            model.addAttribute("importFile", new ImportFile());

            model.addAttribute("title", "Groups management");

            return Views.GROUPS;
        } catch (HttpClientErrorException e) {
            return Views.ERROR_404;
        } catch (HttpServerErrorException e) {
            return Views.ERROR_500;
        }
    }

    @GetMapping(RequestsPath.CREATE_GROUP)
    public String createGroupGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("title", "Create new Group");

        return Views.SEND_REPORT;
    }

    @GetMapping(RequestsPath.EDIT_GROUP)
    public String editGroupGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String id) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("_id", id);

        ResponseEntity<String> infoRes = groupService.findGroupDataById(user.getToken(), id);

        JSONObject jsonObject = new JSONObject(infoRes.getBody()).getJSONObject("data");
        if (jsonObject.getString("idParent").equals(Configs.ROOT_GROUP)) {
            return roleForbidden(redirect);
        }

        model.addAttribute("data", jsonObject.toString());
        model.addAttribute("title", "Edit Group");

        return Views.EDIT_REPORT;
    }

    @GetMapping(RequestsPath.AJAX_GROUPS)
    public ResponseEntity<String> ajaxGroupsGET(HttpSession session, RedirectAttributes redirect,
            @RequestParam String idGroup, @RequestParam("isNext") String isNextStr) {
        if (!SessionUtils.isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        boolean isNext = Boolean.parseBoolean(isNextStr);

        if (isNext) {
            return groupService.findGroupsByIdParentWhenCallAjax(token, idGroup);
        } else {
            Group currentGroup = groupService.findGroupParent(token, "data.idGroup=" + idGroup);
            Group parentGroup = groupService.findGroupParent(token, "data.idGroup=" + currentGroup.getIdParent());
            if (parentGroup == null) {
                return new ResponseEntity<>("[]", HttpStatus.OK);
            }
            return groupService.findGroupsByIdParentWhenCallAjax(token, parentGroup.getIdParent());
        }
    }

    @PostMapping(RequestsPath.READ_GROUPS)
    public String readSurveyPOST(@Valid ImportFile importFile, HttpServletRequest request, HttpSession session,
            RedirectAttributes redirect) {
        redirect.addAttribute("idParent", "root");
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
            List<String> list = groupService.getListGroupsFromFile(path);
            ResponseEntity<String> res = null;
            for (String data : list) {
                res = groupService.insertGroup(token, data);
                if (res.getStatusCode() != HttpStatus.CREATED) {
                    redirect.addFlashAttribute(Keys.IMPORT, false);
                    return "redirect:" + RequestsPath.GROUPS;
                }
            }

            redirect.addFlashAttribute(Keys.IMPORT, true);
            return "redirect:" + RequestsPath.GROUPS;
        } catch (IOException e) {
            redirect.addFlashAttribute(Keys.IMPORT, false);
            return "redirect:" + RequestsPath.GROUPS;
        }
    }
}
