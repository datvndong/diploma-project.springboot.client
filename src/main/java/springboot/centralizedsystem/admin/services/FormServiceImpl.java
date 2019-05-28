package springboot.centralizedsystem.admin.services;

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

import springboot.centralizedsystem.admin.domains.Form;
import springboot.centralizedsystem.admin.domains.FormControl;
import springboot.centralizedsystem.admin.resources.APIs;
import springboot.centralizedsystem.admin.utils.CalculateUtils;
import springboot.centralizedsystem.admin.utils.HttpUtils;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormControlService formControlService;

    @Override
    public List<Form> findAllForms(String token, String email) throws ParseException, ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> res = new RestTemplate().exchange(
                APIs.FORM_URL + "?type=form&sort=created&owner=" + email + "&limit=10&skip=0", HttpMethod.GET, entity,
                String.class);

        JSONArray jsonArray = new JSONArray(res.getBody());
        JSONObject jsonObject = null;
        List<Form> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            String name = jsonObject.getString("name");
            String title = jsonObject.getString("title");
            String path = jsonObject.getString("path");
            int amount = new JSONArray(findAllSubmissions(token, path).getBody()).length();

            // handle: exeption when formControl == null
            FormControl formControl = formControlService.findByPathForm(path);
            String start = formControl.getStart();
            String expired = formControl.getExpired();

            List<String> tags = new ArrayList<>();
            for (Object object : jsonObject.getJSONArray("tags")) {
                tags.add(object.toString());
            }
            int durationPercent = CalculateUtils.getDurationPercent(start, expired);
            String typeProgressBar = CalculateUtils.getTypeProgressBar(durationPercent);
            list.add(new Form(name, title, path, amount, start, expired, tags, durationPercent, typeProgressBar));
        }

        return list;
    }

    @Override
    public ResponseEntity<String> findAllSubmissions(String token, String path)
            throws ResourceAccessException, HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        return new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET, entity, String.class);
    }

    @Override
    public ResponseEntity<String> findOneFormWithToken(String token, String path)
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
    public boolean deleteForm(String token, String path) throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        try {
            HttpHeaders header = HttpUtils.getHeader();
            header.set(APIs.TOKEN_KEY, token);

            HttpEntity<String> entity = new HttpEntity<>(header);

            new RestTemplate().exchange(APIs.modifiedForm(path), HttpMethod.DELETE, entity, String.class);

            return true;
        } catch (HttpClientErrorException | HttpServerErrorException | UnknownHttpStatusCodeException e) {
            return false;
        }
    }

    @Override
    public String findOneFormWithNoToken(String path) {
        // User to get form with Anonymous assign
        return new RestTemplate().getForObject(APIs.getFormByAlias(path), String.class);
    }
}
