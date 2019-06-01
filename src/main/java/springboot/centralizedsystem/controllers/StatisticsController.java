package springboot.centralizedsystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import springboot.centralizedsystem.resources.RequestsPath;
import springboot.centralizedsystem.resources.Views;

@Controller
public class StatisticsController {

    @GetMapping(RequestsPath.STATISTICS)
    public String statisticsGET() {
        return Views.STATISTICS;
    }
}
