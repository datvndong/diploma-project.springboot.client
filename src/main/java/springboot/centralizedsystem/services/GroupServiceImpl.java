package springboot.centralizedsystem.services;

import org.json.JSONArray;
import org.json.JSONObject;
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

import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class GroupServiceImpl implements GroupService {

    @Override
    public String findGroupFiledByIdGroup(String token, String path, String idGroup, String field)
            throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(path) + "?select=data&data.idGroup=" + idGroup;

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        JSONArray dataArray = new JSONArray(res.getBody());
        JSONObject dataObject = dataArray.getJSONObject(0).getJSONObject("data");

        return dataObject.getString(field);
    }

    @Override
    public ResponseEntity<String> findGroupDataById(String token, String path, String id)
            throws ResourceAccessException, HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(path) + "/" + id;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }
}
