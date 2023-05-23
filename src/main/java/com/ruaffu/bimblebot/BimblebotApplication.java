package com.ruaffu.bimblebot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BimblebotApplication {

    @Autowired
    private DiscordBot discordBot;

    public static void main(String[] args) {
        SpringApplication.run(BimblebotApplication.class, args);
    }
    @PostConstruct
    public void init() throws Exception {
        discordBot.startBot();
    }
}
