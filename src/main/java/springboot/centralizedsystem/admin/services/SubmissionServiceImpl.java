package springboot.centralizedsystem.admin.services;

import org.json.JSONArray;
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

import springboot.centralizedsystem.admin.resources.APIs;
import springboot.centralizedsystem.admin.resources.Configs;
import springboot.centralizedsystem.admin.utils.HttpUtils;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Override
    public ResponseEntity<String> findAllSubmissions(String token, String path, int page)
            throws ResourceAccessException, HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(path) + "?select=data&limit=" + Configs.NUMBER_ROWS_PER_PAGE + "&skip="
                + (page - 1) * Configs.NUMBER_ROWS_PER_PAGE;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Override
    public int countSubmissions(String token, String path) throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(path) + "?limit=1000000000&select=data";

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        return new JSONArray(res.getBody()).length();
    }
}
