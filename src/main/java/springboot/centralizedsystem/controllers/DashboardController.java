package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.Errors;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;

@Controller
public class DashboardController {

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model, HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirect.addFlashAttribute(Errors.LOGIN, Messages.TOKEN_EXPIRED_MESSAGE);
            return "redirect:" + RequestsPath.LOGIN;
        }

        model.addAttribute("title", "Dashboard");
        return Views.DASHBOARD;
    }
}
