package springboot.centralizedsystem.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import springboot.centralizedsystem.admin.domains.FormControl;

public interface FormControlRepository extends MongoRepository<FormControl, String> {

    FormControl findByPathForm(String pathForm);

    FormControl findByAssign(String assign);
    
    boolean deleteByPathForm(String pathForm);
}
