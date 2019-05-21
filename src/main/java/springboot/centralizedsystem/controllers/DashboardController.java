package springboot.centralizedsystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;

@Controller
public class DashboardController {

    @GetMapping(RequestsPath.DASHBOARD)
    public String dashboardGET(Model model) {
        model.addAttribute("title", "Dashboard");
        return Views.DASHBOARD;
    }
}
