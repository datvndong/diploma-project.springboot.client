package springboot.centralizedsystem.services;

import java.util.List;

import org.springframework.http.ResponseEntity;

import springboot.centralizedsystem.domains.Form;

public interface StatisticsService {

    List<Form> findFormsCanStatistics(String token, String email);

    ResponseEntity<String> analysisForm(String token, String path);
}
