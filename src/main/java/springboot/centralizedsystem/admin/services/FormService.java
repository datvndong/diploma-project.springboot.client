package springboot.centralizedsystem.admin.services;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.admin.domains.Form;

public interface FormService {

    List<Form> findAllForms(String token, String email) throws ParseException;

    ResponseEntity<String> findAllSubmissions(String token, String path);

    ResponseEntity<String> findOneForm(String token, String path);

    ResponseEntity<String> buildForm(String token, String formJSON, String path);

    boolean deleteForm(String token, String path);
}
