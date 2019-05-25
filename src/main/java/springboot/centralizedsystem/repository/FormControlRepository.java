package springboot.centralizedsystem.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.domains.FormControl;

public interface FormControlRepository extends MongoRepository<FormControl, String> {

    FormControl findByPathForm(String pathForm);

    boolean deleteByPathForm(String pathForm);
}
