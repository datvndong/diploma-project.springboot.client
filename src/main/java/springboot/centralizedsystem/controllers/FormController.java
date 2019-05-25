package springboot.centralizedsystem.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpSession;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormControlService;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.RoleService;
import springboot.centralizedsystem.utils.SessionUtils;
import springboot.centralizedsystem.utils.ValidateUtils;

@Controller
public class FormController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private FormControlService formControlService;

    @GetMapping(RequestsPath.FORMS)
    public String formsGET(ModelMap model, HttpSession session, @ModelAttribute(Keys.DELETE) String deleteMess) {
        try {
            User user = SessionUtils.getUser(session);

            model.addAttribute("list", formService.findAllForms(user.getToken(), user.getEmail()));
            if (!deleteMess.equals("")) {
                boolean isDeleteSuccess = Boolean.parseBoolean(deleteMess);
                model.addAttribute("deleteMess", Messages.DELETE("form", isDeleteSuccess));
                model.addAttribute("deleteStatus", isDeleteSuccess);
            }
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
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Form Builder");
        return Views.BUILD_FORM;
    }

    @GetMapping(RequestsPath.EDIT_FORM)
    public String editFormGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        User user = SessionUtils.getUser(session);
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("pathForm", path);
        model.addAttribute("title", "Form Builder");

        return Views.BUILD_FORM;
    }

    @GetMapping(RequestsPath.BUILDER)
    public String builderGET(ModelMap model, HttpSession session, RedirectAttributes redirect,
            @RequestParam("path") String path) {
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        model.addAttribute("listRoles", roleService.findAll(token));

        JSONObject jsonObject = null;
        if (path.equals("")) {
            // Create form
            jsonObject = new JSONObject();
            jsonObject.put("title", "");
            jsonObject.put("path", "");
            jsonObject.put("name", "");
            jsonObject.put("startDate", "");
            jsonObject.put("startTime", "");
            jsonObject.put("expiredDate", "");
            jsonObject.put("expiredTime", "");
            jsonObject.put("tags", new ArrayList<String>());
            jsonObject.put("components", new ArrayList<String>());
        } else {
            // Edit form
            ResponseEntity<String> res = formService.findOneForm(token, path);
            jsonObject = new JSONObject(res.getBody());
            jsonObject.put("startDate", "2019-05-26");
            jsonObject.put("startTime", "10:05:00");
            jsonObject.put("expiredDate", "2019-05-27");
            jsonObject.put("expiredTime", "11:05:00");
            // Missing Assign
        }
        model.addAttribute("obj", jsonObject.toString());

        return Views.BUILDER;
    }

    @PostMapping(RequestsPath.CREATE_FORM)
    public ResponseEntity<String> createFormPOST(@RequestParam("formJSON") String formJSON, HttpSession session) {
        try {
            User user = SessionUtils.getUser(session);

            JSONObject jsonObject = new JSONObject(formJSON);
            String[] fields = { "title", "path", "name", "startDate", "startTime", "expiredDate", "expiredTime" };
            for (String field : fields) {
                if (ValidateUtils.isEmptyString(jsonObject, field)) {
                    return new ResponseEntity<>("Please fill out `" + field + "` field", HttpStatus.BAD_REQUEST);
                }
            }

            String pathForm = jsonObject.getString("path");
            String assign = jsonObject.getString("assign");

            String startDate = jsonObject.getString("startDate");
            String expiredDate = jsonObject.getString("expiredDate");

            // Compare date, start > expired
            SimpleDateFormat sdf = new SimpleDateFormat(Configs.DATE_FORMAT);
            Date date1 = sdf.parse(startDate);
            Date date2 = sdf.parse(expiredDate);
            if (date1.compareTo(date2) >= 0) {
                return new ResponseEntity<>(Messages.DATE_PICK_ERROR, HttpStatus.BAD_REQUEST);
            }

            // Get start and expired date time to save in database
            String start = startDate + " " + jsonObject.getString("startTime");
            String expired = expiredDate + " " + jsonObject.getString("expiredTime");

            if (!formControlService.insert(new FormControl(pathForm, assign, start, expired))) {
                return new ResponseEntity<>(Messages.DATABASE_ERROR, HttpStatus.BAD_REQUEST);
            }

            return formService.createForm(user.getToken(), formJSON);
        } catch (ParseException e) {
            return new ResponseEntity<>(Messages.FORMAT_DATE_ERROR, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(RequestsPath.DELETE_FORM)
    public String deleteFormDELETE(HttpSession session, RedirectAttributes redirect, @PathVariable String path) {
        User user = SessionUtils.getUser(session);

        boolean isDeleteFormControlSuccess = formControlService.deleteByPathForm(path);
        if (!isDeleteFormControlSuccess) {
            redirect.addFlashAttribute(Keys.DELETE, false);
            return "redirect:" + RequestsPath.FORMS;
        }

        boolean isDeleteFormSuccess = formService.deleteForm(user.getToken(), path);
        redirect.addFlashAttribute(Keys.DELETE, Boolean.toString(isDeleteFormSuccess));

        return "redirect:" + RequestsPath.FORMS;
    }
}
