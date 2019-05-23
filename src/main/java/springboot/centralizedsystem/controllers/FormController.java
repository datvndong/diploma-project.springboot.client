package springboot.centralizedsystem.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;
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
                    String start = "05/18/2019 00:01:00";
                    String end = "05/30/2019 05:15:00";
                    List<String> tags = new ArrayList<>();
                    for (Object object : jsonObject.getJSONArray("tags")) {
                        tags.add(object.toString());
                    }
                    int durationPercent = getDurationPercent(start, end);
                    String typeProgressBar = getTypeProgressBar(durationPercent);
                    list.add(new Form(name, title, path, amount, start, end, tags, durationPercent, typeProgressBar));
                }
            }
            model.addAttribute("list", list);

            model.addAttribute("title", "Forms management");
            return Views.FORMS;
        } catch (NullPointerException | HttpClientErrorException | UnknownHttpStatusCodeException e) {
            redirect.addFlashAttribute(Errors.LOGIN, Messages.TOKEN_EXPIRED_MESSAGE);
            return "redirect:" + RequestsPath.LOGIN;
        } catch (ParseException e) {
            return Views.ERROR_404;
        }
    }

    @GetMapping(RequestsPath.DATAS)
    public ResponseEntity<String> datasGET(@RequestParam("path") String path, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            HttpHeaders header = HttpUtils.getHeader();
            header.set(APIs.TOKEN_KEY, user.getToken());

            HttpEntity<String> entity = new HttpEntity<>(header);

            return new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET, entity, String.class);
        } catch (NullPointerException | HttpClientErrorException | UnknownHttpStatusCodeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(RequestsPath.FORM)
    public ResponseEntity<String> formGET(@RequestParam("path") String path, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            HttpHeaders header = HttpUtils.getHeader();
            header.set(APIs.TOKEN_KEY, user.getToken());

            HttpEntity<String> entity = new HttpEntity<>(header);

            return new RestTemplate().exchange(APIs.getFormByAlias(path), HttpMethod.GET, entity, String.class);
        } catch (NullPointerException | HttpClientErrorException | UnknownHttpStatusCodeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(RequestsPath.CREATE_FORM)
    public String createFormGET(ModelMap model) {
        model.addAttribute("title", "Form Builder");
        return Views.CREATE_FORM;
    }

    @GetMapping(RequestsPath.BUILDER)
    public String buildGET() {
        return Views.BUILDER;
    }

    private int getAmount(HttpEntity<String> entity, String path)
            throws HttpClientErrorException, UnknownHttpStatusCodeException {
        ResponseEntity<String> res = new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET,
                entity, String.class);
        return new JSONArray(res.getBody()).length();
    }

    private int getDurationPercent(String start, String end) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date dateStart = format.parse(start);
        Date dateEnd = format.parse(end);

        // in milliseconds
        long diffStartEnd = dateEnd.getTime() - dateStart.getTime();
        long diffStartNow = new Date().getTime() - dateStart.getTime();

        int result = (int) Math.round((double) diffStartNow / diffStartEnd * 100);
        if (result > 100) {
            result = 100;
        }

        return result;
    }

    private String getTypeProgressBar(int percent) {
        if (percent < 25) {
            return "success";
        } else if (percent < 50) {
            return "primary";
        } else if (percent < 75) {
            return "warning";
        } else if (percent < 100) {
            return "danger";
        }
        return "info";
    }
}
