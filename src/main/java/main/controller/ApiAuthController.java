package main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiAuthController {

    @GetMapping("/api/auth/check")
    public String check() {
        return "{\n"
            + "\"result\": false\n"
            + "}";
    }

}
