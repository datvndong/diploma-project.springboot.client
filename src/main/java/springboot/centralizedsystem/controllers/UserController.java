package springboot.centralizedsystem.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.GroupControl;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.GroupControlService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.services.UserService;
import springboot.centralizedsystem.utils.SessionUtils;
import springboot.centralizedsystem.utils.ValidateUtils;

@Controller
public class UserController extends BaseController {

    private static final String PATH = "user"; // Default resource create by form.io

    @Autowired
    private UserService userService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private GroupControlService groupControlService;

    @GetMapping(RequestsPath.USERS)
    public String usersGET(ModelMap model, HttpSession session, RedirectAttributes redirect, @PathVariable String page,
            @ModelAttribute(Keys.DELETE) String deleteMess) {
        try {
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            List<User> list = new ArrayList<>();

            int sizeListForms = submissionService.countSubmissions(token, PATH);
            int currPage = Integer.parseInt(page);
            int totalPages = (int) Math.ceil((float) sizeListForms / Configs.NUMBER_ROWS_PER_PAGE);

            ResponseEntity<String> userResByPage = submissionService.findSubmissionsByPage(token, PATH, currPage);
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

                String idGroup = dataObject.getString("idGroup");
                GroupControl groupControl = groupControlService.findByIdGroup(idGroup);

                list.add(new User(id, dataObject.getString("email"), dataObject.getString("name"),
                        groupControl.getName(), dataObject.getString("gender"), phoneNumber, address));
            }

            model.addAttribute("list", list);

            if (!deleteMess.equals("")) {
                boolean isDeleteSuccess = Boolean.parseBoolean(deleteMess);
                model.addAttribute("deleteMess", Messages.DELETE("form", isDeleteSuccess));
                model.addAttribute("deleteStatus", isDeleteSuccess);
            }

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
        User user = SessionUtils.getUser(session);

        if (!SessionUtils.isAdmin(session)) {
            return Views.ERROR_403;
        }

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("title", "Create new User");

        return Views.SEND_REPORT;
    }

    @GetMapping(RequestsPath.EDIT_USER)
    public String editUserGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String id) {
        User user = SessionUtils.getUser(session);

        if (!SessionUtils.isAdmin(session)) {
            return Views.ERROR_403;
        }

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("_id", id);

        ResponseEntity<String> infoRes = userService.findUserDataById(user.getToken(), PATH, id);
        model.addAttribute("data", new JSONObject(infoRes.getBody()).getJSONObject("data").toString());

        model.addAttribute("title", "Edit User");

        return Views.EDIT_REPORT;
    }
}
