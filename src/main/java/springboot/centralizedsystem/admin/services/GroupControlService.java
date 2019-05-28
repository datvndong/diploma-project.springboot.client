package springboot.centralizedsystem.admin.services;

import java.util.List;

import springboot.centralizedsystem.admin.domains.GroupControl;

public interface GroupControlService {

    List<GroupControl> findAll();

    boolean insert(GroupControl groupControl);

    GroupControl findByIdGroup(String id);
}
