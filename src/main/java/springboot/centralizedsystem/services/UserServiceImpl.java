package springboot.centralizedsystem.services;

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

import com.google.gson.JsonObject;

import springboot.centralizedsystem.domains.User;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<String> findUserDataById(String token, String path, String id) throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(path) + "/" + id;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Override
    public ResponseEntity<String> updateUserInfo(User user, String path)
            throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, user.getToken());

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("data", new JsonObject());
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        dataObject.addProperty("email", user.getEmail());
        dataObject.addProperty("name", user.getName());
        dataObject.addProperty("idGroup", user.getIdGroup());
        dataObject.addProperty("permission", "user");
        dataObject.addProperty("gender", user.getGender());
        dataObject.addProperty("phoneNumber", user.getPhoneNumber());
        dataObject.addProperty("address", user.getAddress());
        dataObject.addProperty("status", 1);
        dataObject.addProperty("submit", true);

        HttpEntity<String> entity = new HttpEntity<>(dataObject.toString(), header);

        String url = APIs.getListSubmissionsURL(path) + "/" + user.getId();

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.PUT, entity, String.class);

        return res;
    }
}
