package springboot.centralizedsystem.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import springboot.centralizedsystem.admin.resources.RequestsPath;
import springboot.centralizedsystem.admin.resources.Views;

@Controller
public class StatisticsController {

    @GetMapping(RequestsPath.STATISTICS)
    public String statisticsGET() {
        return Views.STATISTICS;
    }
}
