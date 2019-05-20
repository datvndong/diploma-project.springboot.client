package springboot.centralizedsystem.controllers;

import java.util.Arrays;

import javax.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
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
import springboot.centralizedsystem.resources.Pages;

@Controller
public class LoginController {

    private static final String URL_TEST = "http://localhost:3001/current";

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @GetMapping(value = { "/test" })
    public String test(ModelMap model) {
        // HttpHeaders
        HttpHeaders headers = getHeaders();
        headers.set("x-jwt-token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7Il9pZCI6IjVjZGZiOWQ0M2U1MzRlMjVhODI1NWJmMiJ9LCJmb3JtIjp7Il9pZCI6IjVjZGZiOWNjM2U1MzRlMjVhODI1NWJlOCJ9LCJpYXQiOjE1NTgyODAxMzcsImV4cCI6MTU1ODI5NDUzN30.DbIX7dwrnOMioCuIKPyutZUNjXXGF8v2tV1V1HCYTE0");

        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Gửi yêu cầu với phương thức GET, và các thông tin Headers.
        ResponseEntity<String> response = restTemplate.exchange(URL_TEST, HttpMethod.GET, entity, String.class);

        HttpStatus statusCode = response.getStatusCode();
        System.out.println("Response Satus Code: " + statusCode);
        if (statusCode == HttpStatus.OK) {
            String result = response.getBody();
            System.out.println(result);
        }
        return "test";
    }

    @GetMapping(value = { "", "/", "/login" })
    public String loginGET(Model model, @ModelAttribute(Errors.ERROR_401) String error) {
        model.addAttribute("title", "Login");
        model.addAttribute("user", new User("xtreme@admin.io", null, null));
        if (!error.equals("")) {
            model.addAttribute("error", error);
        }
        return Pages.LOGIN;
    }

    @PostMapping("/login")
    public String loginPOST(@Valid User user, Model model, BindingResult result, RedirectAttributes redirect) {
        try {
            if (result.hasErrors()) {
                return Pages.LOGIN;
            }

            String reqJSON = "{\"data\":{\"email\":\"" + user.getUsername() + "\",\"password\":\""
                    + user.getPassword() + "\"}}";

            // Attached data in request.
            HttpEntity<String> entity = new HttpEntity<>(reqJSON, getHeaders());

            // Submit request with POST method.
            ResponseEntity<String> res = new RestTemplate().postForEntity(APIs.LOGIN_URL, entity,
                    String.class);

            // Code = 200.
            if (res.getStatusCode() == HttpStatus.OK) {
                String body = res.getBody();
                user.setToken(res.getHeaders().get(APIs.TOKEN_KEY).get(0));
                System.out.println("-> " + body);
            }

            return Pages.INDEX;
        } catch (HttpClientErrorException e) {
            // 401 Unauthorized
            // 400 Bad Request
            if (e.getMessage().equals(Errors.ERROR_401)) {
                redirect.addFlashAttribute(Errors.ERROR_401, Errors.INVALID_ACCOUNT);
                return "redirect:/" + Pages.LOGIN;
            }
            return Pages.ERROR_404;
        } catch (ResourceAccessException e) {
            // I/O error on POST request for "http://localhost:3001/user/login": Connection
            // refused: connect; nested exception is java.net.ConnectException: Connection
            // refused: connect
            System.err.println(e);
            return Pages.ERROR_404;
        }
    }

    @GetMapping("/logout")
    public String logoutGET() {
        HttpHeaders headers = getHeaders();

        // HttpEntity<String>: To get result as String.
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = new RestTemplate().exchange(APIs.LOGOUT_URL, HttpMethod.GET, entity,
                String.class);

        return response.getStatusCode() == HttpStatus.OK ? "redirect:/" + Pages.LOGIN : Pages.ERROR_404;
    }
}
