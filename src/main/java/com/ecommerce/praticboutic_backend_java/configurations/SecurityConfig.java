package com.ecommerce.praticboutic_backend_java.configurations;

import com.google.cloud.storage.HttpMethod;
import org.apache.catalina.User;
import org.junit.jupiter.api.Disabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import static org.apache.catalina.webresources.TomcatURLStreamHandlerFactory.disable;


@Configuration
    public class SecurityConfig  {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

}

