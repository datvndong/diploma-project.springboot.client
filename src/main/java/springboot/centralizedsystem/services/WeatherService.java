package springboot.centralizedsystem.services;

import org.springframework.http.ResponseEntity;

public interface WeatherService {

    ResponseEntity<String> getWeather(String owmAPIKey, String idCity);
}
