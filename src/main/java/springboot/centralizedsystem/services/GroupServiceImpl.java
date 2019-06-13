package springboot.centralizedsystem.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import com.google.gson.JsonObject;

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

    @Override
    public List<String> getListGroupsFromFile(String pathFile) throws IOException {
        File file = new File(pathFile);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

        // Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Iterate through each rows one by one
        Iterator<Row> rowIterator = sheet.iterator();

        // Prepare variable
        List<String> result = new ArrayList<>();

        Cell cell = null;
        String[] labels = { "idGroup", "name", "idParent", "status" };

        JsonObject userObj = new JsonObject();
        userObj.add("data", new JsonObject());
        JsonObject dataObj = userObj.get("data").getAsJsonObject();

        // Start reading
        boolean isFirstRow = true;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }

            int colIndex = 0;

            // For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                cell = cellIterator.next();
                switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    dataObj.addProperty(labels[colIndex++], cell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    dataObj.addProperty(labels[colIndex++], String.valueOf((int) cell.getNumericCellValue()));
                    break;
                }
            }
            result.add(userObj.toString());
        }

        return result;
    }

    @Override
    public ResponseEntity<String> insertGroup(String token, String data) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(data, header);

        return new RestTemplate().exchange(APIs.getFormByAlias(PATH), HttpMethod.POST, entity, String.class);
    }
}
