package com.ssii.rdp.demogradle;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class DemoController {
    
    @RequestMapping("/")
    public String index() {
        return "Greetings from Kubernetes! testing branch";
    }
}