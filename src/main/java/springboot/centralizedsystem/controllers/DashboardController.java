package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.DashboardService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class DashboardController extends BaseController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        model.addAttribute("groups", dashboardService.findNumberGroups(token));
        model.addAttribute("forms", dashboardService.findNumberForms(user.getEmail(), token));
        model.addAttribute("users", dashboardService.findNumberUsers(token));
        model.addAttribute("city", dashboardService.getCityInfo());
        model.addAttribute("title", "Dashboard");

        return Views.DASHBOARD;
    }
}
