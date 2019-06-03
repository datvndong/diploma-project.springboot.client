package springboot.centralizedsystem.controllers;

import java.util.List;

import javax.servlet.http.HttpSession;

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
            if (!SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);

            String token = user.getToken();

            Group rootGroup = groupService.findRootGroup(token);
            List<Group> list = groupService.findListChildGroupByIdParent(token, rootGroup.getIdGroup(),
                    rootGroup.getName());
            rootGroup.setNumberOfChildrenGroup(list.size());

            model.addAttribute("root", rootGroup);
            model.addAttribute("list", list);

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
}
