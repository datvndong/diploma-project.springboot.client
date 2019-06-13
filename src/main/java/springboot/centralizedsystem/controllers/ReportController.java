package springboot.centralizedsystem.controllers;

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

import springboot.centralizedsystem.domains.Form;
import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormControlService;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.GroupService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.utils.CalculateUtils;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class ReportController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FormControlService formControlService;

    @Autowired
    private GroupService groupService;

    private void addFormToList(String token, List<Form> listForm, List<FormControl> listFormControl)
            throws ParseException {
        for (FormControl formControl : listFormControl) {
            String path = formControl.getPathForm();
            String start = formControl.getStart();
            String expired = formControl.getExpired();

            int durationPercent = CalculateUtils.getDurationPercent(start, expired);
            String typeProgressBar = CalculateUtils.getTypeProgressBar(durationPercent);

            ResponseEntity<String> formRes = formService.findFormWithToken(token, path);
            JSONObject formResJSON = new JSONObject(formRes.getBody());
            String title = formResJSON.getString("title");
            List<String> tags = new ArrayList<>();
            for (Object object : formResJSON.getJSONArray("tags")) {
                tags.add(object.toString());
            }

            ResponseEntity<String> submissionsRes = submissionService.findSubmissionsByPage(token, path, 1);
            JSONArray submissionResJSON = new JSONArray(submissionsRes.getBody());
            boolean isSubmitted = !submissionResJSON.isEmpty();

            boolean isPending = CalculateUtils.isFormPendingOrExpired(start);

            listForm.add(new Form(title, path, start, expired, tags, durationPercent, typeProgressBar, isSubmitted,
                    isPending));
        }
    }

    private void getListFormByIdGroupRecursive(String token, List<Form> listForm, String id) throws ParseException {
        List<FormControl> listFormsGroup = formControlService.findByAssign(id);
        addFormToList(token, listForm, listFormsGroup);

        // Check if idGroup have idParent
        String nextIdParent = groupService.findGroupFiledByIdGroup(token, id, "idParent");

        if (!nextIdParent.equals(Configs.ROOT_GROUP)) {
            getListFormByIdGroupRecursive(token, listForm, nextIdParent);
        }
    }

    private boolean isFormAssignToUser(String token, String assignIdGroup, String formIdGroup) {
        if (assignIdGroup.equals(formIdGroup)) {
            return true;
        }

        String nextIdParent = groupService.findGroupFiledByIdGroup(token, formIdGroup, "idParent");
        if (!nextIdParent.equals(Configs.ROOT_GROUP)) {
            return isFormAssignToUser(token, assignIdGroup, nextIdParent);
        } else {
            return false;
        }
    }

    @GetMapping(RequestsPath.REPORTS)
    public String reportsGET(Model model, HttpSession session, RedirectAttributes redirect, @PathVariable String page)
            throws ParseException {
        try {
            if (SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            if (user == null) {
                return unauthorized(redirect);
            }

            String token = user.getToken();

            List<Form> listAllForms = new ArrayList<>();
            List<Form> listFormsByPage = new ArrayList<>();

            getListFormByIdGroupRecursive(token, listAllForms, user.getIdGroup());

            List<FormControl> listFormsAuth = formControlService.findByAssign(Keys.AUTHENTICATED);
            addFormToList(token, listAllForms, listFormsAuth);

            int numberRowsPerPage = Configs.NUMBER_ROWS_PER_PAGE;
            int sizeListReports = listAllForms.size();

            // Process for Profile page
            user.setReportsNumber(sizeListReports);
            int submittedNumber = 0;
            for (Form form : listAllForms) {
                if (form.getIsSubmitted()) {
                    submittedNumber++;
                }
            }
            user.setSubmittedNumber(submittedNumber);

            int currPage = Integer.parseInt(page);
            int totalPages = (int) Math.ceil((float) sizeListReports / numberRowsPerPage);
            model.addAttribute("currPage", currPage);
            model.addAttribute("totalPages", totalPages);

            int start = (currPage - 1) * numberRowsPerPage;
            int end = currPage * numberRowsPerPage;
            for (int i = start; i < end; i++) {
                if (i == sizeListReports) {
                    break;
                }
                listFormsByPage.add(listAllForms.get(i));
            }
            model.addAttribute("list", listFormsByPage);
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
    public String sendReportAuthGET(Model model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        try {
            if (SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            if (user == null) {
                return unauthorized(redirect);
            }
            String token = user.getToken();

            FormControl formControl = formControlService.findByPathForm(path);
            if (formControl == null) {
                return Views.ERROR_404;
            }
            String assign = formControl.getAssign();

            boolean isFormPending = CalculateUtils.isFormPendingOrExpired(formControl.getStart());
            boolean isFormExpired = !CalculateUtils.isFormPendingOrExpired(formControl.getExpired());
            if (isFormPending || isFormExpired) {
                return Views.ERROR_403;
            }

            if (assign.equals(Keys.AUTHENTICATED) || isFormAssignToUser(token, assign, user.getIdGroup())) {
                ResponseEntity<String> res1 = formService.findFormWithToken(token, path);
                JSONObject resJSON = new JSONObject(res1.getBody());

                ResponseEntity<String> res2 = submissionService.findSubmissionsByPage(token, path, 1);
                boolean isNotSubmitted = new JSONArray(res2.getBody()).isEmpty();
                model.addAttribute("link", isNotSubmitted ? APIs.modifiedForm(path) : "");
                model.addAttribute("title",
                        isNotSubmitted ? resJSON.getString("title") : Messages.HAS_SUBMITTED_MESSAGE);
                model.addAttribute("token", token);

                return Views.SEND_REPORT;
            }

            return Views.ERROR_404;
        } catch (ParseException e) {
            return Views.ERROR_UNKNOWN;
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

    @GetMapping(RequestsPath.SEND_REPORT_ANONYMOUS)
    public String sendReportAnonGET(Model model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        JSONObject formJSON = new JSONObject(formService.findFormWithNoToken(path));
        if (formJSON.isEmpty()) {
            return Views.ERROR_403;
        }

        JSONArray submissionAccessJSON = formJSON.getJSONArray("submissionAccess");
        JSONArray roles = submissionAccessJSON.getJSONObject(4).getJSONArray("roles");
        if (roles.length() == 0 || !roles.get(0).equals(Keys.ANONYMOUS)) {
            return Views.ERROR_404;
        }

        model.addAttribute("title", formJSON.getString("title"));
        model.addAttribute("link", APIs.modifiedForm(path));
        model.addAttribute("token", "");

        return Views.SEND_REPORT;
    }

    @GetMapping(RequestsPath.EDIT_REPORT)
    public String editReportGET(Model model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        try {
            if (SessionUtils.isAdmin(session)) {
                return roleForbidden(redirect);
            }
            User user = SessionUtils.getUser(session);
            if (user == null) {
                return unauthorized(redirect);
            }
            String token = user.getToken();

            FormControl formControl = formControlService.findByPathForm(path);
            if (formControl == null) {
                return Views.ERROR_404;
            }
            String assign = formControl.getAssign();

            boolean isFormPending = CalculateUtils.isFormPendingOrExpired(formControl.getStart());
            boolean isFormExpired = !CalculateUtils.isFormPendingOrExpired(formControl.getExpired());
            if (isFormPending || isFormExpired) {
                return Views.ERROR_403;
            }

            if (assign.equals(Keys.AUTHENTICATED) || isFormAssignToUser(token, assign, user.getIdGroup())) {
                ResponseEntity<String> res1 = formService.findFormWithToken(token, path);
                JSONObject resJSON = new JSONObject(res1.getBody());

                ResponseEntity<String> res2 = submissionService.findSubmissionsByPage(token, path, 1);
                JSONArray jsonArray = new JSONArray(res2.getBody());
                boolean isNotSubmitted = jsonArray.isEmpty();
                if (isNotSubmitted) {
                    model.addAttribute("link", "");
                    model.addAttribute("title", Messages.HAS_NOT_SUBMITTED_MESSAGE);
                    model.addAttribute("token", "");
                } else {
                    model.addAttribute("link", APIs.modifiedForm(path));
                    model.addAttribute("title", resJSON.getString("title"));
                    model.addAttribute("_id", jsonArray.getJSONObject(0).getString("_id"));
                    model.addAttribute("data", jsonArray.getJSONObject(0).getJSONObject("data").toString());
                    model.addAttribute("token", token);
                }

                return Views.EDIT_REPORT;
            }

            return Views.ERROR_404;
        } catch (ParseException e) {
            return Views.ERROR_UNKNOWN;
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
