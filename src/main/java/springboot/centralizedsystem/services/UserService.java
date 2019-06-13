package springboot.centralizedsystem.services;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.User;

public interface UserService {

    ResponseEntity<String> findUserDataById(String token, String path, String id);

    ResponseEntity<String> updateUserInfo(User user, String path);

    long countUsers(String token, String idGroup);

    ResponseEntity<String> findUsersByPageAndIdGroup(String token, String idGroup, int page);

    long countUsersByName(String token, String keyword);

    ResponseEntity<String> findUsersByPageAndName(String token, String keyword, int page);

    List<String> getListUsersFromFile(String pathFile) throws IOException;

    ResponseEntity<String> insertUser(String token, String data);
}
