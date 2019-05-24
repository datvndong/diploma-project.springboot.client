package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;

@Controller
public class DashboardController extends BaseController {

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return unauthorized(redirect);
        }

        model.addAttribute("title", "Dashboard");
        return Views.DASHBOARD;
    }
}
