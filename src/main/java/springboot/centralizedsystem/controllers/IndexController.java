package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import springboot.centralizedsystem.resources.Views;

@Controller
public class IndexController {

    @GetMapping("/index")
    public String index(Model model, HttpSession session) {
//        User user = (User) session.getAttribute("user");
//        model.addAttribute("username", user.getUsername());
        return Views.INDEX;
    }
}
