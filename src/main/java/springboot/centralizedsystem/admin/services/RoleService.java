package springboot.centralizedsystem.admin.services;

import java.util.List;

import springboot.centralizedsystem.admin.domains.Role;

public interface RoleService {

    List<Role> findAll(String token);

    Role findOne(String token, String _id);
}
