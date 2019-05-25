package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.repository.FormControlRepository;

@Service
public class FormControlServiceImpl implements FormControlService {

    @Autowired
    private FormControlRepository formRepository;

    @Override
    public List<FormControl> findAll() {
        return this.formRepository.findAll();
    }

    @Override
    public boolean insert(FormControl formControl) {
        try {
            this.formRepository.insert(formControl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteAll() {
        try {
            this.formRepository.deleteAll();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FormControl findOneByPath(String pathForm) {
        return this.formRepository.findByPathForm(pathForm);
    }
}
