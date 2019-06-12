package springboot.centralizedsystem.services;

import springboot.centralizedsystem.domains.City;

public interface DashboardService {

    City getCityInfo();

    long findNumberGroups(String token);

    long findNumberForms(String email, String token);

    long findNumberUsers(String token);
}
