package com.ssii.rdp.demogradle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class DemoGradleApplication {
	@RequestMapping("/")
	public String index(){
		return "Hello Gradle! 2019/5/10";
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoGradleApplication.class, args);
	}

}

