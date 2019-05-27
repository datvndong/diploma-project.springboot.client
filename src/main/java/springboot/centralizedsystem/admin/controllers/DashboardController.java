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
public class DashboardController extends BaseController {

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model, HttpSession session, RedirectAttributes redirect) {
        User admin = SessionUtils.getAdmin(session);
        if (admin == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Dashboard");
        return Views.DASHBOARD;
    }
}
