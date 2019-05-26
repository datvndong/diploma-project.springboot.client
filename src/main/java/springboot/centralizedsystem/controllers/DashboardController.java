package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.Admin;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class DashboardController extends BaseController {

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model, HttpSession session, RedirectAttributes redirect) {
        Admin admin = SessionUtils.getAdmin(session);
        if (admin == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Dashboard");
        return Views.DASHBOARD;
    }
}
