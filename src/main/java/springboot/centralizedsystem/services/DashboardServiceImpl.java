package springboot.centralizedsystem.services;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import springboot.centralizedsystem.domains.City;
import springboot.centralizedsystem.resources.Configs;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private FormControlService formControlService;

    @Override
    public City getCityInfo() {
        String owmAPIKey = "cf76b373a6c28e3253b49e1a8f04beb7";
        String idCity = "1580541";

        ResponseEntity<String> weatherRes = weatherService.getWeather(owmAPIKey, idCity);
        JSONObject weatherObj = new JSONObject(weatherRes.getBody());

        Date currDate = new Date();

        String weekday = new SimpleDateFormat(Configs.WEEKDAY_FORMAT).format(currDate);
        String date = new SimpleDateFormat(Configs.WEATHER_DATE_FORMAT).format(currDate);
        String name = weatherObj.getString("name");
        String country = weatherObj.getJSONObject("sys").getString("country");
        double temperature = weatherObj.getJSONObject("main").getInt("temp") - 273.15; // convert *K to *C
        double temperatureFormat = Double.parseDouble(new DecimalFormat("##.##").format(temperature));
        String description = weatherObj.getJSONArray("weather").getJSONObject(0).getString("description");
        String descriptionFormat = description.substring(0, 1).toUpperCase() + description.substring(1);

        return new City(weekday, date, name, country, temperatureFormat, descriptionFormat);
    }

    @Override
    public long findNumberGroups(String token) {
        return submissionService.countSubmissions(token, "group");
    }

    @Override
    public long findNumberForms(String email, String token) {
        return formControlService.findByOwner(email).size();
    }

    @Override
    public long findNumberUsers(String token) {
        return submissionService.countSubmissions(token, "user");
    }
}
