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

import springboot.centralizedsystem.resources.APIs;
import springboot.centralizedsystem.utils.HttpUtils;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Override
    public ResponseEntity<String> getWeather(String owmAPIKey, String idCity) throws ResourceAccessException,
            HttpClientErrorException, HttpServerErrorException, UnknownHttpStatusCodeException {
        HttpHeaders header = HttpUtils.getHeader();

        HttpEntity<String> entity = new HttpEntity<>(header);

        return new RestTemplate().exchange(APIs.getWeather(owmAPIKey, idCity), HttpMethod.GET, entity, String.class);
    }
}
