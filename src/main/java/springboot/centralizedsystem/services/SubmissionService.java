package springboot.centralizedsystem.services;

import org.springframework.http.ResponseEntity;

public interface SubmissionService {

    ResponseEntity<String> findSubmissionsByPage(String token, String path, int page);

    long countSubmissions(String token, String path);

    ResponseEntity<String> findAllSubmissions(String token, String path, boolean isGetOnlyData);
}
