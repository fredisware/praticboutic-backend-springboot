package com.ecommerce.praticboutic_backend_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
//@EnableJpaRepositories(basePackages = "com.ecommerce.praticboutic_backend_java.repositories")
public class PraticbouticBackendJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraticbouticBackendJavaApplication.class, args);
	}
	
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
      return String.format("Hello %s! avec git", name);
    }

}
