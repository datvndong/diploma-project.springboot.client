package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import springboot.centralizedsystem.domains.Form;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private FormService formService;

    @Autowired
    private SubmissionService submissionService;

    private void countValue(JsonArray jsonArray, JsonObject data, String typeComponent) {
        JsonElement valueObj = null;
        JsonArray bluePrintDatasArr = null;
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            String key = obj.get("key").getAsString();
            valueObj = data.get(key);
            if (valueObj == null) {
                continue;
            }

            switch (typeComponent) {
            case "checkbox":
                if (valueObj.getAsBoolean()) {
                    int count = obj.get("count").getAsInt() + 1;
                    obj.addProperty("count", count);
                }
                break;
            case "selectboxes":
                JsonObject resDataObj = valueObj.getAsJsonObject();
                bluePrintDatasArr = obj.get("data").getAsJsonArray();
                for (JsonElement dataElement : bluePrintDatasArr) {
                    JsonObject dataObj = dataElement.getAsJsonObject();
                    valueObj = resDataObj.get(dataObj.get("key").getAsString());
                    if (valueObj != null && valueObj.getAsBoolean()) {
                        int count = dataObj.get("count").getAsInt() + 1;
                        dataObj.addProperty("count", count);
                    }
                }
                break;
            case "select":
            case "radio":
                String resDataValue = valueObj.getAsString();
                bluePrintDatasArr = obj.get("data").getAsJsonArray();
                for (JsonElement dataElement : bluePrintDatasArr) {
                    JsonObject dataObj = dataElement.getAsJsonObject();
                    if (dataObj.get("key").getAsString().equals(resDataValue)) {
                        int count = dataObj.get("count").getAsInt() + 1;
                        dataObj.addProperty("count", count);
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public List<Form> findFormsCanStatistics(String token, String email) {
        return formService.findFormsCanStatistics(token, email);
    }

    @Override
    public ResponseEntity<String> analysisForm(String token, String path) {
        JsonObject analysis = new JsonObject();

        ResponseEntity<String> formsRes = formService.findFormWithToken(token, path);
        JsonObject formResObj = new JsonParser().parse(formsRes.getBody()).getAsJsonObject();
        JsonArray components = formResObj.getAsJsonArray("components");
        for (JsonElement jsonElement : components) {
            JsonObject compObj = jsonElement.getAsJsonObject();
            String type = compObj.get("type").getAsString();
            boolean isValidType = type.equals("checkbox") || type.equals("selectboxes") || type.equals("select")
                    || type.equals("radio");
            if (!isValidType) {
                continue;
            }

            if (!analysis.has(type)) {
                analysis.add(type, new JsonArray());
            }
            JsonArray typesArr = analysis.getAsJsonArray(type);

            JsonObject jsonObject = new JsonObject();
            if (type.equals("checkbox")) {
                jsonObject.addProperty("label", compObj.get("label").getAsString());
                jsonObject.addProperty("key", compObj.get("key").getAsString());
                jsonObject.addProperty("count", 0);
            } else {
                // select || selectboxes || radio
                jsonObject.addProperty("label", compObj.get("label").getAsString());
                jsonObject.addProperty("key", compObj.get("key").getAsString());
                jsonObject.add("data", new JsonArray());

                JsonArray datas = type.equals("select")
                        ? compObj.get("data").getAsJsonObject().get("values").getAsJsonArray()
                        : compObj.get("values").getAsJsonArray();
                for (JsonElement dataElement : datas) {
                    JsonObject dataObj = dataElement.getAsJsonObject();
                    dataObj.addProperty("key", dataObj.get("value").getAsString());
                    dataObj.remove("value");
                    dataObj.addProperty("count", 0);
                    jsonObject.get("data").getAsJsonArray().add(dataObj);
                }
            }
            typesArr.add(jsonObject);
        }

        ResponseEntity<String> submissionsRes = submissionService.findAllSubmissions(token, path, true);
        JsonArray submissionsResArr = new JsonParser().parse(submissionsRes.getBody()).getAsJsonArray();
        analysis.addProperty("amount", submissionsResArr.size());
        JsonArray jsonArray = null;
        for (JsonElement jsonElement : submissionsResArr) {
            JsonObject data = jsonElement.getAsJsonObject().get("data").getAsJsonObject();

            if (analysis.get("checkbox") != null) {
                jsonArray = analysis.get("checkbox").getAsJsonArray();
                countValue(jsonArray, data, "checkbox");
            }

            if (analysis.get("selectboxes") != null) {
                jsonArray = analysis.get("selectboxes").getAsJsonArray();
                countValue(jsonArray, data, "selectboxes");
            }

            if (analysis.get("select") != null) {
                jsonArray = analysis.get("select").getAsJsonArray();
                countValue(jsonArray, data, "select");
            }

            if (analysis.get("radio") != null) {
                jsonArray = analysis.get("radio").getAsJsonArray();
                countValue(jsonArray, data, "radio");
            }
        }

        return new ResponseEntity<>(analysis.toString(), HttpStatus.OK);
    }
}
