package springboot.centralizedsystem.services;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.User;

public interface UserService {

    ResponseEntity<String> findUserDataById(String token, String path, String id);

    ResponseEntity<String> updateUserInfo(User user, String path);
}
