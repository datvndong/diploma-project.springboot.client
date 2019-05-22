package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Errors;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.HttpUtils;

@Controller
public class LoginController {

    @GetMapping(value = { RequestsPath.NONE, RequestsPath.SLASH, RequestsPath.LOGIN })
    public String loginGET(Model model, @ModelAttribute(Errors.LOGIN) String error) {
        model.addAttribute("title", "Login");
        model.addAttribute("user", new User("xtreme@admin.io", null, null));
        if (!error.equals("")) {
            model.addAttribute("error", error);
        }
        return Views.LOGIN;
    }

    @PostMapping(RequestsPath.LOGIN)
    public String loginPOST(@Valid User user, Model model, BindingResult result, HttpSession session,
            RedirectAttributes redirect) {
        try {
            if (result.hasErrors()) {
                return Views.LOGIN;
            }

            String email = user.getEmail();
            String reqJSON = "{\"data\":{\"email\":\"" + email + "\",\"password\":\""
                    + user.getPassword() + "\"}}";

            HttpEntity<String> entity = new HttpEntity<>(reqJSON, HttpUtils.getHeader());

            ResponseEntity<String> res = new RestTemplate().postForEntity(APIs.LOGIN_URL, entity,
                    String.class);

            if (res.getStatusCode() == HttpStatus.OK) {
                session.setAttribute("user",
                        new User(email.split("@")[0], null, res.getHeaders().get(APIs.TOKEN_KEY).get(0)));
                return "redirect:" + RequestsPath.DASHBOARD;
            }

            return Views.ERROR_404;
        } catch (HttpClientErrorException e) {
            // 401 Unauthorized
            // 400 Bad Request
            if (e.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
                redirect.addFlashAttribute(Errors.LOGIN, Messages.INVALID_ACCOUNT_MESSAGE);
                return "redirect:" + RequestsPath.LOGIN;
            }
            return Views.ERROR_404;
        } catch (ResourceAccessException e) {
            // I/O error on POST request for "http://localhost:3001/user/login": Connection
            // refused: connect; nested exception is java.net.ConnectException: Connection
            // refused: connect
            System.err.println(e);
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.LOGOUT)
    public String logoutGET() {
        HttpHeaders header = HttpUtils.getHeader();

        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> response = new RestTemplate().exchange(APIs.LOGOUT_URL, HttpMethod.GET, entity,
                String.class);

        return response.getStatusCode() == HttpStatus.OK ? "redirect:" + RequestsPath.LOGIN : Views.ERROR_404;
    }
}
