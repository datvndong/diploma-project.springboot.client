package springboot.centralizedsystem.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.domains.FormControl;

public interface FormControlRepository extends MongoRepository<FormControl, String> {

    FormControl findByPathForm(String pathForm);

    List<FormControl> findByAssign(String assign);

    List<FormControl> findByOwner(String owner);

    boolean deleteByPathForm(String pathForm);
}
