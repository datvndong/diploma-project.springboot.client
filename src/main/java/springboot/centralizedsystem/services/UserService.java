package springboot.centralizedsystem.services;

import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<String> findUserDataById(String token, String path, String id);
}
