package springboot.centralizedsystem.admin.services;

import java.util.ArrayList;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import springboot.centralizedsystem.admin.domains.Role;
import springboot.centralizedsystem.admin.resources.APIs;
import springboot.centralizedsystem.admin.utils.HttpUtils;

@Service
public class RoleServiceImpl implements RoleService {

    @Override
    public List<Role> findAll(String token) throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> res = new RestTemplate().exchange(APIs.ROLE_URL, HttpMethod.GET, entity,
                String.class);

        JSONArray jsonArray = new JSONArray(res.getBody());
        JSONObject jsonObject = null;
        List<Role> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = (JSONObject) jsonArray.get(i);
            if (jsonObject.getBoolean("admin")) {
                continue;
            }
            String _id = jsonObject.getString("_id");
            String title = jsonObject.getString("title");
            list.add(new Role(_id, title, null, null));
        }

        return list;
    }

    @Override
    public Role findOne(String token, String _id) throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        ResponseEntity<String> res = new RestTemplate().exchange(APIs.ROLE_URL + "?_id=" + _id, HttpMethod.GET, entity,
                String.class);

        JSONObject jsonObject = new JSONObject(res.getBody());
        String title = jsonObject.getString("title");

        return new Role(_id, title, null, null);
    }
}
