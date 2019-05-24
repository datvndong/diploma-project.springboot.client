package springboot.centralizedsystem.controllers;

import java.text.ParseException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.RoleService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class FormController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private RoleService roleService;

    @GetMapping(RequestsPath.FORMS)
    public String formsGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        try {
            User user = SessionUtils.getUser(session);

            model.addAttribute("list", formService.findAllForms(user.getToken(), user.get_id()));
            model.addAttribute("title", "Forms management");

            return Views.FORMS;
        } catch (ParseException e) {
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.SUBMISSIONS)
    public ResponseEntity<String> submissionsGET(@RequestParam("path") String path, HttpSession session) {
        User user = SessionUtils.getUser(session);

        return formService.findAllSubmissions(user.getToken(), path);
    }

    @GetMapping(RequestsPath.FORM)
    public ResponseEntity<String> formGET(@RequestParam("path") String path, HttpSession session) {
        User user = SessionUtils.getUser(session);

        return formService.findOneForm(user.getToken(), path);
    }

    @GetMapping(RequestsPath.CREATE_FORM)
    public String createFormGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        User user = SessionUtils.getUser(session);
        // check token exception
        user.getToken();

        model.addAttribute("title", "Form Builder");
        return Views.CREATE_FORM;
    }

    @GetMapping(RequestsPath.BUILDER)
    public String builderGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        User user = SessionUtils.getUser(session);

        model.addAttribute("listRoles", roleService.findAll(user.getToken()));

        return Views.BUILDER;
    }

    @PostMapping(RequestsPath.CREATE_FORM)
    public ResponseEntity<String> createFormPOST(@RequestParam("formJSON") String formJSON, HttpSession session) {
        User user = SessionUtils.getUser(session);

        return formService.createForm(user.getToken(), formJSON);
    }
}
