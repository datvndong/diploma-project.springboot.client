package springboot.centralizedsystem.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import springboot.centralizedsystem.domains.Form;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class FormServiceImpl implements FormService {

    private int getAmount(HttpEntity<String> entity, String path)
            throws HttpClientErrorException, UnknownHttpStatusCodeException {
        ResponseEntity<String> res = new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET,
                entity, String.class);
        return new JSONArray(res.getBody()).length();
    }

    private int getDurationPercent(String start, String end) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    @Override
    public List<Form> findAllForms(String token, String _id) throws ParseException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> res = new RestTemplate().exchange(APIs.FORM_URL, HttpMethod.GET, entity,
                String.class);

        JSONArray jsonArray = new JSONArray(res.getBody());
        JSONObject jsonObject = null;
        List<Form> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            if (jsonObject.isNull("owner") || !jsonObject.getString("owner").equals(_id)
                    || !jsonObject.getString("type").equals("form")) {
                continue;
            }
            String name = jsonObject.getString("name");
            String title = jsonObject.getString("title");
            String path = jsonObject.getString("path");
            int amount = getAmount(entity, path);
            String start = "2019-05-18 00:01:00";
            String end = "2019-05-30 05:15:00";
            List<String> tags = new ArrayList<>();
            for (Object object : jsonObject.getJSONArray("tags")) {
                tags.add(object.toString());
            }
            int durationPercent = getDurationPercent(start, end);
            String typeProgressBar = getTypeProgressBar(durationPercent);
            list.add(new Form(name, title, path, amount, start, end, tags, durationPercent, typeProgressBar));
        }

        return list;
    }

    @Override
    public ResponseEntity<String> findAllSubmissions(String token, String path)
            throws HttpClientErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        return new RestTemplate().exchange(APIs.getListSubmissionsURL(path), HttpMethod.GET, entity, String.class);
    }

    @Override
    public ResponseEntity<String> findOneForm(String token, String path)
            throws HttpClientErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        return new RestTemplate().exchange(APIs.getFormByAlias(path), HttpMethod.GET, entity, String.class);
    }

    @Override
    public ResponseEntity<String> createForm(String token, String formJSON)
            throws HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(formJSON, header);

        return new RestTemplate().postForEntity(APIs.FORM_URL, entity, String.class);
    }
}
