package springboot.centralizedsystem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.domains.Group;

public interface GroupRepository extends MongoRepository<Group, String> {

    Group findByIdGroup(String id);
}
