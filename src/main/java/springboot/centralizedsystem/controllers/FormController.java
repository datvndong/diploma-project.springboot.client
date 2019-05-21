package springboot.centralizedsystem.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springboot.centralizedsystem.domains.Form;
import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Errors;
import springboot.centralizedsystem.resources.Messages;
import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;
import springboot.centralizedsystem.utils.HttpUtils;

@Controller
public class FormController {

    @GetMapping(RequestsPath.FORMS)
    public String formsGET(ModelMap model, HttpSession session, RedirectAttributes redirect) {
        try {
            User user = (User) session.getAttribute("user");
            HttpHeaders header = HttpUtils.getHeader();
            header.set(APIs.TOKEN_KEY, user.getToken());

            HttpEntity<String> entity = new HttpEntity<>(header);

            ResponseEntity<String> res = new RestTemplate().exchange(APIs.LIST_FORMS_URL, HttpMethod.GET, entity,
                    String.class);

            if (res.getStatusCode() == HttpStatus.OK) {
                JSONArray jsonArray = new JSONArray(res.getBody());
                JSONObject jsonObject = null;
                List<Form> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = (JSONObject) jsonArray.get(i);
                    if (jsonObject.get("type").equals("form")) {
                        String name = jsonObject.getString("name");
                        String title = jsonObject.getString("title");
                        String path = jsonObject.getString("path");
                        int amount = getAmount(entity, path);
                        list.add(new Form(name, title, path, amount, "", "May 15, 2015", "", ""));
                    }
                }
                model.addAttribute("title", "Forms management");
                model.addAttribute("list", list);
            }
            return Views.FORMS;
        } catch (NullPointerException e) {
            redirect.addFlashAttribute(Errors.LOGIN, Messages.TOKEN_EXPIRED_MESSAGE);
            return "redirect:" + RequestsPath.LOGIN;
        }
    }

    private int getAmount(HttpEntity<String> entity, String path) throws NullPointerException {
        ResponseEntity<String> res = new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET,
                entity, String.class);
        if (res.getStatusCode() == HttpStatus.OK) {
            return new JSONArray(res.getBody()).length();
        }
        return -1;
    }
}
