package springboot.centralizedsystem.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.admin.domains.GroupControl;

public interface GroupControlRepository extends MongoRepository<GroupControl, String> {

}
