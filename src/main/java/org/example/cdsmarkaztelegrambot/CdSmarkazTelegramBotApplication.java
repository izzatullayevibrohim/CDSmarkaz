package org.example.cdsmarkaztelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CdSmarkazTelegramBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdSmarkazTelegramBotApplication.class, args);
    }

}
