package springboot.centralizedsystem.controllers;

import java.text.ParseException;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.UnknownHttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Errors;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.RoleService;

@Controller
public class FormController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private RoleService roleService;

    @GetMapping(RequestsPath.FORMS)
    public String formsGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        try {
            User user = (User) session.getAttribute("user");

            model.addAttribute("list", formService.findAllForms(user.getToken(), user.get_id()));
            model.addAttribute("title", "Forms management");

            return Views.FORMS;
        } catch (ParseException e) {
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.SUBMISSIONS)
    public ResponseEntity<String> submissionsGET(@RequestParam("path") String path, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");

            return formService.findAllSubmissions(user.getToken(), path);
        } catch (UnknownHttpStatusCodeException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (HttpClientErrorException | HttpServerErrorException httpException) {
            String error = httpException.getResponseBodyAsString();
            return new ResponseEntity<>(new JSONObject(error).getString("message"), httpException.getStatusCode());
        }
    }

    @GetMapping(RequestsPath.FORM)
    public ResponseEntity<String> formGET(@RequestParam("path") String path, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");

            return formService.findOneForm(user.getToken(), path);
        } catch (NullPointerException | HttpClientErrorException | UnknownHttpStatusCodeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(RequestsPath.CREATE_FORM)
    public String createFormGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            redirect.addFlashAttribute(Errors.LOGIN, Messages.TOKEN_EXPIRED_MESSAGE);
            return "redirect:" + RequestsPath.LOGIN;
        }

        model.addAttribute("title", "Form Builder");
        return Views.CREATE_FORM;
    }

    @GetMapping(RequestsPath.BUILDER)
    public String builderGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        try {
            User user = (User) session.getAttribute("user");

            model.addAttribute("listRoles", roleService.findAll(user.getToken()));

            return Views.BUILDER;
        } catch (UnknownHttpStatusCodeException e) {
            redirect.addFlashAttribute(Errors.LOGIN, Messages.TOKEN_EXPIRED_MESSAGE);
            return "redirect:" + RequestsPath.LOGIN;
        }
    }

    @PostMapping(RequestsPath.CREATE_FORM)
    public ResponseEntity<String> createFormPOST(@RequestParam("formJSON") String formJSON, HttpSession session) {
        ResponseEntity<String> result = null;
        try {
            User user = (User) session.getAttribute("user");
            result = formService.createForm(user.getToken(), formJSON);
            return result;
        } catch (UnknownHttpStatusCodeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
