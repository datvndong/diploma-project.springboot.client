package springboot.centralizedsystem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.domains.GroupControl;

public interface GroupControlRepository extends MongoRepository<GroupControl, String> {

    GroupControl findByIdGroup(String id);
}
