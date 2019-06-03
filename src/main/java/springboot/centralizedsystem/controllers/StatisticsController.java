package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class StatisticsController extends BaseController {

    @GetMapping(RequestsPath.STATISTICS)
    public String statisticsGET(Model model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        return Views.STATISTICS;
    }
}
