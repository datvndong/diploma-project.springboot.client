package springboot.centralizedsystem.admin.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.admin.domains.Admin;
import springboot.centralizedsystem.admin.resources.APIs;
import springboot.centralizedsystem.admin.resources.Keys;
import springboot.centralizedsystem.admin.resources.Messages;
import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;
import springboot.centralizedsystem.admin.utils.HttpUtils;

@Controller
public class LoginController {

    @GetMapping(value = { RequestsPath.NONE, RequestsPath.SLASH, RequestsPath.LOGIN })
    public String loginGET(Model model, @ModelAttribute(Keys.LOGIN) String error) {
        model.addAttribute("title", "Login");
        model.addAttribute("boss", new Admin(null, "xtreme@admin.io", null, null));
        if (!error.equals("")) {
            model.addAttribute("error", error);
        }
        return Views.LOGIN;
    }

    @PostMapping(RequestsPath.LOGIN)
    public String loginPOST(@Valid Admin boss, Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            String email = boss.getEmail();
            String reqJSON = "{\"data\":{\"email\":\"" + email + "\",\"password\":\"" + boss.getPassword() + "\"}}";

            HttpEntity<String> entity = new HttpEntity<>(reqJSON, HttpUtils.getHeader());

            ResponseEntity<String> res = new RestTemplate().postForEntity(APIs.LOGIN_URL, entity, String.class);

            session.setAttribute(Keys.ADMIN,
                    new Admin(email.split("@")[0], email, null, res.getHeaders().get(APIs.TOKEN_KEY).get(0)));

            return "redirect:" + RequestsPath.DASHBOARD;
        } catch (HttpClientErrorException httpException) {
            redirect.addFlashAttribute(Keys.LOGIN, Messages.INVALID_ACCOUNT_ERROR);
            return "redirect:" + RequestsPath.LOGIN;
        } catch (ResourceAccessException resourceException) {
            // I/O error on POST request for "http://localhost:3001/user/login": Connection
            // refused: connect; nested exception is java.net.ConnectException: Connection
            // refused: connect
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.LOGOUT)
    public String logoutGET(HttpSession session) {
        session.invalidate();

        return "redirect:" + RequestsPath.LOGIN;
    }
}
