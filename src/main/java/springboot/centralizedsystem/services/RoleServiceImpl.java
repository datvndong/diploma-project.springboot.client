package springboot.centralizedsystem.services;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import springboot.centralizedsystem.domains.Role;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class RoleServiceImpl implements RoleService {

    @Override
    public List<Role> findAll(String token) throws HttpClientErrorException, UnknownHttpStatusCodeException {
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
}
