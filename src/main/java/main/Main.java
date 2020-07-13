package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        System.setProperty("spring.thymeleaf.cache", "false");
        SpringApplication.run(Main.class,args);
    }
}
