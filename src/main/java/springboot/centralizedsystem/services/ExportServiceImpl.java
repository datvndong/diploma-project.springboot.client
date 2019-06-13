package springboot.centralizedsystem.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import springboot.centralizedsystem.resources.Configs;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private SubmissionService submissionService;

    private String convertToCSV(String[] data) {
        return Stream.of(data).collect(Collectors.joining(","));
    }

    @Override
    public String exportSubmissionDatasToString(String token, String path, String type) {
        StringBuilder str = new StringBuilder();

        if (type.equals(Configs.JSON)) {
            // Write JSON file
            ResponseEntity<String> submissionRes = submissionService.findAllSubmissions(token, path, false);
            JSONArray jsonArray = new JSONArray(submissionRes.getBody());
            for (Object object : jsonArray) {
                str.append(object.toString());
                str.append(System.getProperty("line.separator"));
            }
        } else {
            // Write CSV file
            ResponseEntity<String> submissionRes = submissionService.findAllSubmissions(token, path, false);
            JSONArray jsonArray = new JSONArray(submissionRes.getBody());
            int size = jsonArray.length();
            JSONObject jsonObject = null;
            JSONObject dataObject = null;
            List<String[]> dataLines = new ArrayList<>();
            List<String> line = null;
            Iterator<String> iterator = null;
            boolean isFirstWrite = true;

            for (int i = 0; i < size; i++) {
                jsonObject = jsonArray.getJSONObject(i);

                // Add row header
                if (isFirstWrite) {
                    line = new ArrayList<>();
                    line.add("_id");
                    line.add("created");
                    line.add("modified");

                    iterator = jsonObject.getJSONObject("data").keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        line.add(key);
                    }

                    dataLines.add(line.toArray(new String[line.size()]));

                    isFirstWrite = false;
                }

                // Add row data
                line = new ArrayList<>();

                line.add(jsonObject.getString("_id"));
                line.add(jsonObject.getString("created"));
                line.add(jsonObject.getString("modified"));

                dataObject = jsonObject.getJSONObject("data");
                iterator = dataObject.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    String value = dataObject.get(key).toString();
                    line.add(value);
                }

                dataLines.add(line.toArray(new String[line.size()]));
            }

            for (String[] data : dataLines) {
                str.append(convertToCSV(data));
                str.append(System.getProperty("line.separator"));
            }
        }

        return str.toString();
    }
}
