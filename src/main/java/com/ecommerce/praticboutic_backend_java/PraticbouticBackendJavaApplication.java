package com.ecommerce.praticboutic_backend_java;

import com.ecommerce.praticboutic_backend_java.configurations.ClientUrlsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
@EnableConfigurationProperties(ClientUrlsProperties.class)
//@EnableJpaRepositories(basePackages = "com.ecommerce.praticboutic_backend_java.repositories")
public class PraticbouticBackendJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraticbouticBackendJavaApplication.class, args);
	}
	
    @PostMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
      return String.format("Hello %s! avec git", name);
    }

}
