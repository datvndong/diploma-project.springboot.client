package springboot.centralizedsystem.controllers;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
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
import springboot.centralizedsystem.services.FormService;
import springboot.centralizedsystem.services.SubmissionService;
import springboot.centralizedsystem.utils.SessionUtils;

@Controller
public class StatisticsController extends BaseController {

    @Autowired
    private FormService formService;

    @Autowired
    private SubmissionService submissionService;

    @GetMapping(RequestsPath.STATISTICS)
    public String statisticsGET(Model model, HttpSession session, RedirectAttributes redirect) {
        if (!SessionUtils.isAdmin(session)) {
            return roleForbidden(redirect);
        }
        User user = SessionUtils.getUser(session);

        model.addAttribute("list", formService.findFormsCanStatistics(user.getToken(), user.getEmail()));

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

        JSONObject result = new JSONObject();

        ResponseEntity<String> formsRes = formService.findFormWithToken(token, path);
        JSONObject formResObj = new JSONObject(formsRes.getBody());
        JSONArray components = formResObj.getJSONArray("components");
        int compSize = components.length();
        for (int i = 0; i < compSize; i++) {
            JSONObject compObj = components.getJSONObject(i);
            String type = compObj.getString("type");
            boolean isValidType = type.equals("checkbox") || type.equals("selectboxes") || type.equals("select")
                    || type.equals("radio");
            if (!isValidType) {
                continue;
            }

            if (!result.has(type)) {
                result.put(type, new JSONArray());
            }
            JSONArray typesArr = result.getJSONArray(type);

            switch (type) {
            case "checkbox":
                JSONObject checkboxObj = new JSONObject();
                checkboxObj.put("label", compObj.getString("label"));
                checkboxObj.put("key", compObj.getString("key"));
                checkboxObj.put("count", 0);
                typesArr.put(checkboxObj);
                break;
            case "select":
                JSONArray selectsArr = compObj.getJSONObject("data").getJSONArray("values");
                for (Object object : selectsArr) {
                    JSONObject selectObj = (JSONObject) object;
                    selectObj.put("count", 0);
                    typesArr.put(selectObj);
                }
                break;
            case "selectboxes":
            case "radio":
                JSONArray othersArr = compObj.getJSONArray("values");
                for (Object object : othersArr) {
                    JSONObject otherObj = (JSONObject) object;
                    otherObj.put("count", 0);
                    typesArr.put(otherObj);
                }
                break;
            }
        }

        ResponseEntity<String> submissionsRes = submissionService.findAllSubmissions(token, path);
        JSONArray submissionsResArr = new JSONArray(submissionsRes.getBody());
        int arrSize = submissionsResArr.length();
        result.put("amount", arrSize);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
