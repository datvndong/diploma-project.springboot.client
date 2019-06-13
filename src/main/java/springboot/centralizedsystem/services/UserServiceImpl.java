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
import springboot.centralizedsystem.resources.Configs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final String PATH_USER = "user";

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

    @Override
    public long countUsers(String token, String idGroup) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH_USER) + "?limit=" + Configs.LIMIT_QUERY + "&data.idGroup="
                + idGroup + "&select=_id";

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        return new JSONArray(res.getBody()).length();
    }

    @Override
    public ResponseEntity<String> findUsersByPageAndIdGroup(String token, String idGroup, int page) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH_USER) + "?select=data&limit=" + Configs.NUMBER_ROWS_PER_PAGE
                + "&skip=" + (page - 1) * Configs.NUMBER_ROWS_PER_PAGE + "&data.idGroup=" + idGroup;

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Override
    public long countUsersByName(String token, String keyword) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH_USER) + "?limit=" + Configs.LIMIT_QUERY + "&data.name__regex=/"
                + keyword + "/&select=_id";

        ResponseEntity<String> res = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

        return new JSONArray(res.getBody()).length();
    }

    @Override
    public ResponseEntity<String> findUsersByPageAndName(String token, String keyword, int page) {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(header);

        String url = APIs.getListSubmissionsURL(PATH_USER) + "?select=data&limit=" + Configs.NUMBER_ROWS_PER_PAGE
                + "&skip=" + (page - 1) * Configs.NUMBER_ROWS_PER_PAGE + "&data.name__regex=/" + keyword + "/";

        return new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Override
    public List<String> getListUsersFromFile(String pathFile) throws IOException {
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
        String[] labels = { "email", "name", "idGroup", "permission", "gender", "phoneNumber", "address", "status" };

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
                    dataObj.addProperty(labels[colIndex++], (int) cell.getNumericCellValue());
                    break;
                }
            }
            result.add(userObj.toString());
        }

        return result;
    }

    @Override
    public ResponseEntity<String> insertUser(String token, String data) throws ResourceAccessException, HttpClientErrorException,
            HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();
        header.set(APIs.TOKEN_KEY, token);

        HttpEntity<String> entity = new HttpEntity<>(data, header);

        return new RestTemplate().exchange(APIs.getFormByAlias(PATH_USER), HttpMethod.POST, entity, String.class);
    }
}
