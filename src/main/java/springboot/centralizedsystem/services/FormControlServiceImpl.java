package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.repository.FormControlRepository;

@Service
public class FormControlServiceImpl implements FormControlService {

    @Autowired
    private FormControlRepository formControlRepository;

    @Override
    public List<FormControl> findAll() {
        return this.formControlRepository.findAll();
    }

    @Override
    public boolean insert(FormControl formControl) {
        try {
            this.formControlRepository.insert(formControl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteAll() {
        try {
            this.formControlRepository.deleteAll();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FormControl findByPathForm(String pathForm) {
        return this.formControlRepository.findByPathForm(pathForm);
    }

    @Override
    public boolean deleteByPathForm(String pathForm) {
        try {
            FormControl formControl = formControlRepository.findByPathForm(pathForm);
            this.formControlRepository.delete(formControl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
