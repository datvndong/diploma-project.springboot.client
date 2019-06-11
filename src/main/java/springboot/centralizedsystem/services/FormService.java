package springboot.centralizedsystem.services;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.Form;

public interface FormService {

    List<Form> findForms(String token, String email, int page) throws ParseException;

    ResponseEntity<String> findFormWithToken(String token, String path);

    String findFormWithNoToken(String path);

    ResponseEntity<String> buildForm(String token, String formJSON, String path);

    boolean deleteForm(String token, String path);

    List<Form> findFormsCanStatistics(String token, String email);
}
