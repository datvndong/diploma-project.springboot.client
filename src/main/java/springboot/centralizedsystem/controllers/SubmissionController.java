package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class SubmissionController extends BaseController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FormService formService;

    @GetMapping(RequestsPath.SUBMISSIONS)
    public String submissionsGET(Model model, HttpSession session, @PathVariable String path,
            RedirectAttributes redirect, @PathVariable String page) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        int sizeListForms = submissionService.countSubmissions(token, path);
        int currPage = Integer.parseInt(page);
        int totalPages = (int) Math.ceil((float) sizeListForms / Configs.NUMBER_ROWS_PER_PAGE);
        model.addAttribute("currPage", currPage);
        model.addAttribute("totalPages", totalPages);

        ResponseEntity<String> submissionRes = submissionService.findSubmissionsByPage(token, path, currPage);
        model.addAttribute("submissionData", submissionRes.getBody());

        ResponseEntity<String> formRes = formService.findOneFormWithToken(user.getToken(), path);
        model.addAttribute("formData", formRes.getBody());

        model.addAttribute("path", path);
        model.addAttribute("title", "Submissions");

        return Views.SUBMISSIONS;
    }
}
