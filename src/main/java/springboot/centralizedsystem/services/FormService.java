package springboot.centralizedsystem.services;

import java.text.ParseException;
import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.Form;

public interface FormService {

    List<Form> findAllForms(String token, String _id) throws ParseException;

    ResponseEntity<String> findAllSubmissions(String token, String path);

    ResponseEntity<String> findOneForm(String token, String path);

    ResponseEntity<String> createForm(String token, String formJSON);
}
