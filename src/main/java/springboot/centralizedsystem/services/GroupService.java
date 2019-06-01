package springboot.centralizedsystem.services;

import org.springframework.http.ResponseEntity;

public interface GroupService {

    String findGroupFiledByIdGroup(String token, String path, String idGroup, String field);

    ResponseEntity<String> findGroupDataById(String token, String path, String id);
}
