package org.example.workshop_managment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Workshop Management API is running";
    }

    @GetMapping("/healthz")
    public String health() {
        return "ok";
    }
}
