package springboot.centralizedsystem.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.admin.domains.FormControl;

public interface FormControlRepository extends MongoRepository<FormControl, String> {

    FormControl findByPathForm(String pathForm);

    boolean deleteByPathForm(String pathForm);
}
