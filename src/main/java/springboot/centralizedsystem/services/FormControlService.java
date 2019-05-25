package springboot.centralizedsystem.services;

import java.util.List;

import springboot.centralizedsystem.domains.FormControl;

public interface FormControlService {

    List<FormControl> findAll();

    boolean insert(FormControl formControl);

    boolean deleteAll();

    FormControl findByPathForm(String pathForm);

    boolean deleteByPathForm(String pathForm);
}
