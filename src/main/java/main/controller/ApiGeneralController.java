package main.controller;

import java.sql.SQLOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiGeneralController {
    @Value("${init.title}")
    private String title;
    @Value("${init.subtitle}")
    private String subtitle;
    @Value("${init.phone}")
    private String phone;
    @Value("${init.email}")
    private String mail;
    @Value("${init.copyright}")
    private String copyright;
    @Value("${init.copyrightFrom}")
    private String copyrightFrom;


    @GetMapping("/api/init")
    public String init() {
        System.out.println();
        return "{"
            + "\"title\": \""+title+"\","
            + "\"subtitle\": \""+subtitle+"\","
            + "\"phone\": \""+phone+"\","
            + "\"email\": \""+mail+"\","
            + "\"copyright\": \""+copyright+"\","
            + "\"copyrightFrom\": \""+copyrightFrom+"\""
            + "}";

    }

    @GetMapping("/api/settings")
    public String settings() {

        return "{\n"
            + "\"MULTIUSER_MODE\": false,\n"
            + "\"POST_PREMODERATION\": true,\n"
            + "\"STATISTICS_IS_PUBLIC\": null\n"
            + "}";
    }


}
