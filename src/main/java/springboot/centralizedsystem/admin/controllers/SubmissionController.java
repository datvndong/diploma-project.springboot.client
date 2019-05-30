package springboot.centralizedsystem.admin.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import springboot.centralizedsystem.admin.domains.User;
import springboot.centralizedsystem.admin.resources.Configs;
import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;
import springboot.centralizedsystem.admin.services.FormService;
import springboot.centralizedsystem.admin.services.SubmissionService;
import springboot.centralizedsystem.admin.utils.SessionUtils;

@Controller
public class SubmissionController extends BaseController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FormService formService;

    @GetMapping(RequestsPath.SUBMISSIONS)
    public String submissionsGET(Model model, HttpSession session, @PathVariable String path,
            @PathVariable String page) {
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
