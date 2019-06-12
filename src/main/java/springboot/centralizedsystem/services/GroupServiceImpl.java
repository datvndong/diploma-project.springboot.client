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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import springboot.centralizedsystem.domains.Group;
import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class GroupServiceImpl implements GroupService {

    private static final String PATH = "group";

    @Override
    public String findGroupFiledByIdGroup(String token, String idGroup, String field)
            throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH) + "?select=data&data.idGroup=" + idGroup;

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        JSONArray dataArray = new JSONArray(res.getBody());
        JSONObject dataObject = dataArray.getJSONObject(0).getJSONObject("data");

        return dataObject.getString(field);
    }

    @Override
    public ResponseEntity<String> findGroupDataById(String token, String id) throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException,
            UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH) + "/" + id;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Override
    public Group findGroupParent(String token, String condition) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH) + "?select=data&" + condition;

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        JSONArray dataArray = new JSONArray(res.getBody());
        if (dataArray.length() == 0) {
            return null;
        }
        JSONObject jsonObject = dataArray.getJSONObject(0);
        JSONObject dataObject = jsonObject.getJSONObject("data");

        String id = jsonObject.getString("_id");
        String idGroup = dataObject.getString("idGroup");
        String name = dataObject.getString("name");
        String idParent = dataObject.getString("idParent");
        String nameParent = "";

        return new Group(id, idGroup, name, idParent, nameParent);
    }

    @Override
    public List<Group> findListChildGroupByIdParentWithPage(String token, String idParent, String nameParent,
            int page) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH);
        if (page > 0) {
            url += "?limit=" + Configs.NUMBER_ROWS_PER_PAGE + "&skip=" + (page - 1) * Configs.NUMBER_ROWS_PER_PAGE;
        } else {
            // If page = 0 => get full data
            url += "?limit=" + Configs.LIMIT_QUERY;
        }
        url += "&sort=-create&select=data&data.status=" + Configs.ACTIVE_STATUS + "&data.idParent=" + idParent;

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        List<Group> listGroups = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(res.getBody());
        JSONObject jsonObject = null;
        JSONObject dataObject = null;
        int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            jsonObject = jsonArray.getJSONObject(i);
            dataObject = jsonObject.getJSONObject("data");

            String id = jsonObject.getString("_id");
            String idGroup = dataObject.getString("idGroup");
            String name = dataObject.getString("name");
            int childSize = findNumberOfChildGroupByIdParent(token, idGroup);
            listGroups.add(new Group(id, idGroup, name, idParent, nameParent, childSize));
        }
        return listGroups;
    }

    @Override
    public int findNumberOfChildGroupByIdParent(String token, String idParent) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH) + "?limit=" + Configs.LIMIT_QUERY
                + "&select=_id&data.status=" + Configs.ACTIVE_STATUS + "&data.idParent=" + idParent;

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        return new JSONArray(res.getBody()).length();
    }

    @Override
    public ResponseEntity<String> findGroupsByIdParentWhenCallAjax(String token, String idParent) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH) + "?limit=" + Configs.LIMIT_QUERY
                + "&sort=-create&select=data&data.status=" + Configs.ACTIVE_STATUS + "&data.idParent=" + idParent;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }
}
