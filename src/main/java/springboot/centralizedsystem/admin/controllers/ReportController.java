package springboot.centralizedsystem.admin.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.admin.domains.User;
import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;
import springboot.centralizedsystem.admin.utils.SessionUtils;

@Controller
public class ReportController extends BaseController {

    @GetMapping(RequestsPath.REPORTS)
    public String reportsGET(Model model, HttpSession session, RedirectAttributes redirect) {
        User user = SessionUtils.getUser(session);
        if (SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Reports");
        return Views.REPORTS;
    }
}
