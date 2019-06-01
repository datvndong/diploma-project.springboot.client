package springboot.centralizedsystem.services;

import java.util.List;

import springboot.centralizedsystem.domains.GroupControl;

public interface GroupControlService {

    List<GroupControl> findAll();

    boolean insert(GroupControl groupControl);

    GroupControl findByIdGroup(String id);
}
