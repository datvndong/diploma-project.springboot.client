package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Errors;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.HttpUtils;

@Controller
public class LoginController {

    @GetMapping(value = { RequestsPath.NONE, RequestsPath.SLASH, RequestsPath.LOGIN })
    public String loginGET(Model model, @ModelAttribute(Errors.LOGIN) String error) {
        model.addAttribute("title", "Login");
        model.addAttribute("user", new User(null, "xtreme@admin.io", null, null));
        if (!error.equals("")) {
            model.addAttribute("error", error);
        }
        return Views.LOGIN;
    }

    @PostMapping(RequestsPath.LOGIN)
    public String loginPOST(@Valid User user, Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            String email = user.getEmail();
            String reqJSON = "{\"data\":{\"email\":\"" + email + "\",\"password\":\"" + user.getPassword() + "\"}}";

            HttpEntity<String> entity = new HttpEntity<>(reqJSON, HttpUtils.getHeader());

            ResponseEntity<String> res = new RestTemplate().postForEntity(APIs.LOGIN_URL, entity, String.class);

            JSONObject body = new JSONObject(res.getBody());

            session.setAttribute("user", new User(body.getString("_id"), email.split("@")[0], null,
                    res.getHeaders().get(APIs.TOKEN_KEY).get(0)));

            return "redirect:" + RequestsPath.DASHBOARD;
        } catch (ResourceAccessException e) {
            // I/O error on POST request for "http://localhost:3001/user/login": Connection
            // refused: connect; nested exception is java.net.ConnectException: Connection
            // refused: connect
            System.err.println(e);
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.LOGOUT)
    public String logoutGET(HttpSession session) {
        session.invalidate();

        return "redirect:" + RequestsPath.LOGIN;
    }
}
