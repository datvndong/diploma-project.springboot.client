package springboot.centralizedsystem.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import springboot.centralizedsystem.domains.Form;
import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.resources.Keys;
import springboot.centralizedsystem.utils.CalculateUtils;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FormControlService formControlService;

    @Override
    public List<Form> findForms(String token, String email, int page) throws ParseException, ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String apiURL = APIs.FORM_URL + "?type=form&sort=-created&owner=" + email + "&limit=" + Configs.NUMBER_ROWS_PER_PAGE
                + "&skip=" + (page - 1) * Configs.NUMBER_ROWS_PER_PAGE;
        ResponseEntity<String> res = new RestTemplate().exchange(apiURL, HttpMethod.GET, entity, String.class);

        JSONArray jsonArray = new JSONArray(res.getBody());
        int size = jsonArray.length();
        JSONObject jsonObject = null;
        List<Form> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            String title = jsonObject.getString("title");
            String path = jsonObject.getString("path");
            long amount = submissionService.countSubmissions(token, path);

            // handle: exception when formControl == null
            FormControl formControl = formControlService.findByPathForm(path);
            String start = formControl.getStart();
            String expired = formControl.getExpired();
            String assign = formControl.getAssign();

            List<String> tags = new ArrayList<>();
            for (Object object : jsonObject.getJSONArray("tags")) {
                tags.add(object.toString());
            }
            int durationPercent = CalculateUtils.getDurationPercent(start, expired);
            String typeProgressBar = CalculateUtils.getTypeProgressBar(durationPercent);
            list.add(new Form(name, title, path, amount, start, expired, tags, durationPercent, typeProgressBar,
                    assign.equals(Keys.ANONYMOUS)));
        }

        return list;
    }

    @Override
    public ResponseEntity<String> findFormWithToken(String token, String path)
            throws ResourceAccessException, HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        return new RestTemplate().exchange(APIs.getFormByAlias(path), HttpMethod.GET, entity, String.class);
    }

    @Override
    public ResponseEntity<String> buildForm(String token, String formJSON, String path) throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(formJSON, header);

        if (path.equals("")) {
            // Create
            return new RestTemplate().postForEntity(APIs.FORM_URL, entity, String.class);
        }
        // Edit
        return new RestTemplate().exchange(APIs.modifiedForm(path), HttpMethod.PUT, entity, String.class);
    }

    @Override
    public boolean deleteForm(String token, String path) {
        try {
            HttpHeaders header = HttpUtils.getHeader();
            header.set(APIs.TOKEN_KEY, token);

            HttpEntity<String> entity = new HttpEntity<>(header);

            new RestTemplate().exchange(APIs.modifiedForm(path), HttpMethod.DELETE, entity, String.class);

            return true;
        } catch (ResourceAccessException | HttpClientErrorException | HttpServerErrorException
                | UnknownHttpStatusCodeException e) {
            return false;
        }
    }

    @Override
    public String findFormWithNoToken(String path) {
        // User to get form with Anonymous assign
        return new RestTemplate().getForObject(APIs.getFormByAlias(path), String.class);
    }

    @Override
    public List<Form> findFormsCanStatistics(String token, String email) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String apiURL = APIs.FORM_URL + "?type=form&sort=-created&owner=" + email + "&limit=" + Configs.LIMIT_QUERY
                + "&select=title,path,components";
        ResponseEntity<String> res = new RestTemplate().exchange(apiURL, HttpMethod.GET, entity, String.class);

        JSONArray jsonArray = new JSONArray(res.getBody());
        int size = jsonArray.length();
        JSONObject jsonObject = null;
        List<Form> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            jsonObject = jsonArray.getJSONObject(i);
            JSONArray components = jsonObject.getJSONArray("components");
            int compSize = components.length();
            for (int j = 0; j < compSize; j++) {
                JSONObject compObj = components.getJSONObject(j);
                String type = compObj.getString("type");
                if (type.equals("checkbox") || type.equals("selectboxes") || type.equals("select")
                        || type.equals("radio")) {
                    String title = jsonObject.getString("title");
                    String path = jsonObject.getString("path");
                    list.add(new Form(title, path));
                    break;
                }
            }
        }

        return list;
    }
}
