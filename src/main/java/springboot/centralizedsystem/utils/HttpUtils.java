package springboot.centralizedsystem.utils;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpUtils {

    public static HttpHeaders getHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));
        header.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return header;
    }
}
