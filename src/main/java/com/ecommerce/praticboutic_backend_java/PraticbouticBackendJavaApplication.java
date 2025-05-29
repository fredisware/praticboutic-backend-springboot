package com.ecommerce.praticboutic_backend_java;

import com.ecommerce.praticboutic_backend_java.configurations.ClientUrlsProperties;
import com.ecommerce.praticboutic_backend_java.services.ExecMacroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(ClientUrlsProperties.class)
@EnableConfigServer
public class PraticbouticBackendJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraticbouticBackendJavaApplication.class, args);
	}
	
    @PostMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
      return String.format("Hello %s! avec git", name);
    }

    @Autowired
    private ExecMacroService execMacroService;

    @Bean
    public CommandLineRunner testBean() {
        return args -> {
            System.out.println("ExecMacroService bean: " + execMacroService);
        };
    }

}
