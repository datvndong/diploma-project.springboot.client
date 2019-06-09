package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.Group;

public interface GroupService {

    Group findGroupParent(String token, String condition);

    List<Group> findListChildGroupByIdParentWithPage(String token, String idParent, String nameParent, int page);

    int findNumberOfChildGroupByIdParent(String token, String idParent);

    String findGroupFiledByIdGroup(String token, String idGroup, String field);

    ResponseEntity<String> findGroupDataById(String token, String id);

    ResponseEntity<String> findGroupsByIdParentWhenCallAjax(String token, String idParent);
}
