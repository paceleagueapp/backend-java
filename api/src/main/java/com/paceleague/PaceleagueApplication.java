package com.paceleague;

import com.paceleague.common.config.JwtProperties;
import com.paceleague.crew.config.CrewProperties;
import com.paceleague.territory.config.TerritoryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({JwtProperties.class, TerritoryProperties.class, CrewProperties.class})
@SpringBootApplication
public class PaceleagueApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaceleagueApplication.class, args);
    }

}
