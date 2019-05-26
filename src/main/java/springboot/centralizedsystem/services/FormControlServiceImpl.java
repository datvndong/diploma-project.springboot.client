package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.WriteResult;

import springboot.centralizedsystem.domains.FormControl;
import springboot.centralizedsystem.repository.FormControlRepository;

@Service
public class FormControlServiceImpl implements FormControlService {

    @Autowired
    private FormControlRepository formControlRepository;

    @Autowired
    MongoTemplate mongoTemplate;

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

    @Override
    public int update(FormControl formControl, String oldPath) {
        Query query = new Query(Criteria.where("pathForm").is(oldPath));

        Update update = new Update();
        update.set("pathForm", formControl.getPathForm());
        update.set("assign", formControl.getAssign());
        update.set("start", formControl.getStart());
        update.set("expired", formControl.getExpired());

        WriteResult result = this.mongoTemplate.updateFirst(query, update, FormControl.class);
        return result.getN();
    }
}
