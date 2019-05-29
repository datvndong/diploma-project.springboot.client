package springboot.centralizedsystem.admin.services;

import org.springframework.http.ResponseEntity;

public interface SubmissionService {

    ResponseEntity<String> findAllSubmissions(String token, String path, int page);

    int countSubmissions(String token, String path);
}
