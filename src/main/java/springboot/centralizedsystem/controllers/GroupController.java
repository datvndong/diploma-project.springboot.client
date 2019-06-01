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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.Group;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.GroupService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class GroupController extends BaseController {

    private static final String PATH = "group";

    @Autowired
    private GroupService groupService;

    @Autowired
    private SubmissionService submissionService;

    @GetMapping(RequestsPath.GROUPS)
    public String usersGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String page) {
        try {
            User user = SessionUtils.getUser(session);
            String token = user.getToken();

            List<Group> list = new ArrayList<>();

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
                dataObject = jsonObject.getJSONObject("data");

                if (dataObject.getInt("status") == Configs.DEACTIVE_STATUS) {
                    continue;
                }

                String id = jsonObject.getString("_id");
                String idGroup = dataObject.getString("idGroup");
                String name = dataObject.getString("name");
                String idParent = dataObject.getString("idParent");
                String nameParent = "";
                if (!idParent.equals(Configs.ROOT_GROUP)) {
                    nameParent = groupService.findGroupFiledByIdGroup(token, PATH, idParent, "name");
                }

                list.add(new Group(id, idGroup, name, idParent, nameParent));
            }

            model.addAttribute("list", list);

            model.addAttribute("currPage", currPage);
            model.addAttribute("totalPages", totalPages);
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
        User user = SessionUtils.getUser(session);

        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("title", "Create new Group");

        return Views.SEND_REPORT;
    }

    @GetMapping(RequestsPath.EDIT_GROUP)
    public String editGroupGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String id) {
        User user = SessionUtils.getUser(session);

        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }

        model.addAttribute("link", APIs.modifiedForm(PATH));
        model.addAttribute("token", user.getToken());
        model.addAttribute("_id", id);

        ResponseEntity<String> infoRes = groupService.findGroupDataById(user.getToken(), PATH, id);
        model.addAttribute("data", new JSONObject(infoRes.getBody()).getJSONObject("data").toString());

        model.addAttribute("title", "Edit Group");

        return Views.EDIT_REPORT;
    }
}
