package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.services.StatisticsService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class StatisticsController extends BaseController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping(RequestsPath.STATISTICS)
    public String statisticsGET(Model model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("list", statisticsService.findFormsCanStatistics(user.getToken(), user.getEmail()));

        return Views.STATISTICS;
    }

    @GetMapping(RequestsPath.STATISTICAL_ANALYSIS)
    public ResponseEntity<String> analysisGET(Model model, HttpSession session, RedirectAttributes redirect,
            @PathVariable String path) {
        if (!SessionUtils.isAdmin(session)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User user = SessionUtils.getUser(session);
        String token = user.getToken();

        return statisticsService.analysisForm(token, path);
    }
}
