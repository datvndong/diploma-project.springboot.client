package springboot.centralizedsystem.admin.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.admin.domains.Form;
import springboot.centralizedsystem.admin.domains.FormControl;
import springboot.centralizedsystem.admin.domains.GroupControl;
import springboot.centralizedsystem.admin.domains.User;
import springboot.centralizedsystem.admin.resources.APIs;
import springboot.centralizedsystem.admin.resources.Keys;
import springboot.centralizedsystem.admin.resources.Messages;
import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;
import springboot.centralizedsystem.admin.services.FormControlService;
import springboot.centralizedsystem.admin.services.FormService;
import springboot.centralizedsystem.admin.services.GroupControlService;
import springboot.centralizedsystem.admin.utils.CalculateUtils;
import springboot.centralizedsystem.admin.utils.SessionUtils;

@Controller
public class ReportController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormControlService formControlService;

    @Autowired
    private GroupControlService groupControlService;

    private void addFormToList(String token, List<Form> listForm, List<FormControl> listFormControl)
            throws ParseException {
        for (FormControl formControl : listFormControl) {
            String path = formControl.getPathForm();
            String start = formControl.getStart();
            String expired = formControl.getExpired();

            int durationPercent = CalculateUtils.getDurationPercent(start, expired);
            String typeProgressBar = CalculateUtils.getTypeProgressBar(durationPercent);

            ResponseEntity<String> formRes = formService.findOneForm(token, path);
            JSONObject formResJSON = new JSONObject(formRes.getBody());
            String title = formResJSON.getString("title");
            List<String> tags = new ArrayList<>();
            for (Object object : formResJSON.getJSONArray("tags")) {
                tags.add(object.toString());
            }

            ResponseEntity<String> submissionsRes = formService.findAllSubmissions(token, path);
            JSONArray submissionResJSON = new JSONArray(submissionsRes.getBody());
            boolean isSubmitted = !submissionResJSON.isEmpty();

            boolean isPending = CalculateUtils.isFormPending(start);

            listForm.add(new Form(title, path, start, expired, tags, durationPercent, typeProgressBar, isSubmitted,
                    isPending));
        }
    }

    private void getListFormByIdGroupRecursive(String token, List<Form> listForm, String id) throws ParseException {
        List<FormControl> listFormsGroup = formControlService.findByAssign(id);
        addFormToList(token, listForm, listFormsGroup);

        // Check if idGroup have idParent
        GroupControl groupControl = groupControlService.findByIdGroup(id);
        String nextIdParent = groupControl.getIdParent();
        if (!nextIdParent.equals("")) {
            getListFormByIdGroupRecursive(token, listForm, nextIdParent);
        }
    }

    @GetMapping(RequestsPath.REPORTS)
    public String reportsGET(Model model, HttpSession session, RedirectAttributes redirect) throws ParseException {
        try {
            User user = SessionUtils.getUser(session);
            if (SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            if (user == null) {
                return unauthorized(redirect);
            }

            String token = user.getToken();

            List<Form> listForm = new ArrayList<>();

//            List<FormControl> listFormsGroup = formControlService.findByAssign(idGroup);
//            addFormToList(token, listForm, listFormsGroup);

            getListFormByIdGroupRecursive(token, listForm, user.getIdGroup());

            List<FormControl> listFormsAuth = formControlService.findByAssign(Keys.AUTHENTICATED);
            addFormToList(token, listForm, listFormsAuth);

//            List<FormControl> listFormsAnon = formControlService.findByAssign(Keys.ANONYMOUS);
//            addFormToList(token, listForm, listFormsAnon);

            model.addAttribute("list", listForm);
            model.addAttribute("title", "Reports");

            return Views.REPORTS;
        } catch (HttpClientErrorException e) {
            switch (e.getStatusCode()) {
            case NOT_FOUND:
                return Views.ERROR_404;
            default:
                return Views.ERROR_UNKNOWN;
            }
        } catch (HttpServerErrorException e) {
            switch (e.getStatusCode()) {
            case INTERNAL_SERVER_ERROR:
                return Views.ERROR_500;
            default:
                return Views.ERROR_UNKNOWN;
            }
        }
    }

    @GetMapping(RequestsPath.SEND_REPORT_AUTHENTICATED)
    public String sendReportGET(Model model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        try {
            User user = SessionUtils.getUser(session);
            if (user == null) {
                return unauthorized(redirect);
            }
            String token = user.getToken();

            FormControl formControl = formControlService.findByPathForm(path);
            if (formControl == null) {
                return Views.ERROR_404;
            }
            if (formControl.getAssign().equals(Keys.AUTHENTICATED)) {
                ResponseEntity<String> res1 = formService.findOneForm(token, path);
                JSONObject resJSON = new JSONObject(res1.getBody());

                ResponseEntity<String> res2 = formService.findAllSubmissions(token, path);
                boolean isNotSubmitted = new JSONArray(res2.getBody()).isEmpty();
                model.addAttribute("link", isNotSubmitted ? APIs.modifiedForm(path) : "");
                model.addAttribute("title",
                        isNotSubmitted ? resJSON.getString("title") : Messages.HAS_SUBMITTED_MESSAGE);

                return Views.SEND_REPORT;
            }

            return Views.SEND_REPORT;
        } catch (HttpClientErrorException e) {
            switch (e.getStatusCode()) {
            case UNAUTHORIZED:
                return Views.ERROR_403;
            case NOT_FOUND:
                return Views.ERROR_404;
            default:
                return Views.ERROR_UNKNOWN;
            }
        } catch (HttpServerErrorException e) {
            switch (e.getStatusCode()) {
            case INTERNAL_SERVER_ERROR:
                return Views.ERROR_500;
            default:
                return Views.ERROR_UNKNOWN;
            }
        }
    }
}
