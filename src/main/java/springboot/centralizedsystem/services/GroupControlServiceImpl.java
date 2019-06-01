package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import springboot.centralizedsystem.domains.GroupControl;
import springboot.centralizedsystem.repository.GroupControlRepository;

@Service
public class GroupControlServiceImpl implements GroupControlService {

    @Autowired
    private GroupControlRepository groupControlRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<GroupControl> findAll() {
        try {
            return this.groupControlRepository.findAll();
        } catch (Exception e) {
            System.err.println("[ERROR] Find list GroupControl: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean insert(GroupControl groupControl) {
        try {
            this.groupControlRepository.insert(groupControl);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] Insert GroupControl: " + e.getMessage());
            return false;
        }
    }

    @Override
    public GroupControl findByIdGroup(String id) {
        try {
            return this.groupControlRepository.findByIdGroup(id);
        } catch (Exception e) {
            System.err.println("[ERROR] Find one GroupControl by idParent: " + e.getMessage());
            return null;
        }
    }
}
