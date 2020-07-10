package main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiPostController {


    @GetMapping("/api/post")
    public String post(@RequestParam(required = false) String offset,@RequestParam(required = false) String limit, @RequestParam(required = false) String mode) {
        return mode + limit + offset;
    }

    @GetMapping("/api/tag")
    public String tag(){
        return "";
    }
}
