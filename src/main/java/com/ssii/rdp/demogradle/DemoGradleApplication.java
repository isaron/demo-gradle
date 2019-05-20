package com.ssii.rdp.demogradle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class DemoGradleApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoGradleApplication.class, args);
	}

}

