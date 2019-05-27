package springboot.centralizedsystem.admin.services;

import java.util.List;

import springboot.centralizedsystem.admin.domains.FormControl;

public interface FormControlService {

    List<FormControl> findAll();

    boolean insert(FormControl formControl);

    boolean deleteAll();

    FormControl findByPathForm(String pathForm);

    boolean deleteByPathForm(String pathForm);

    int update(FormControl formControl, String oldPath);
}