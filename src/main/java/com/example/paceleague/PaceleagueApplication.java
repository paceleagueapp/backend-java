package com.example.paceleague;

import com.example.paceleague.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class PaceleagueApplication {
 
    public static void main(String[] args) {
        SpringApplication.run(PaceleagueApplication.class, args);
    }

}
