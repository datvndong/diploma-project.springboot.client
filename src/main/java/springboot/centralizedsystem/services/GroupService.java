package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.Group;

public interface GroupService {

    Group findRootGroup(String token);

    List<Group> findListChildGroupByIdParent(String token, String idParent, String nameParent);

    int findNumberOfChildGroupByIdParent(String token, String idParent, String nameParent);

    String findGroupFiledByIdGroup(String token, String idGroup, String field);

    ResponseEntity<String> findGroupDataById(String token, String id);
}
