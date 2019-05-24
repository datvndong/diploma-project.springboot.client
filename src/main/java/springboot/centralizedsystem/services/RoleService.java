package springboot.centralizedsystem.services;

import java.util.List;

import springboot.centralizedsystem.domains.Role;

public interface RoleService {

    List<Role> findAll(String token);
}
