package springboot.centralizedsystem.admin.services;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.admin.domains.Form;

public interface FormService {

    List<Form> findAllForms(String token, String email, int page) throws ParseException;

    ResponseEntity<String> findOneFormWithToken(String token, String path);

    String findOneFormWithNoToken(String path);

    ResponseEntity<String> buildForm(String token, String formJSON, String path);

    boolean deleteForm(String token, String path);
}
