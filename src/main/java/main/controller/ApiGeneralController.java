package main.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import main.model.GlobalSettings;
import main.repositories.GlobalSettingsRepository;
import main.service.GeneralService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ApiGeneralController {
    @Autowired private GeneralService generalService;

    @GetMapping("/api/init")
    public String init() {
        return generalService.init();
    }

    @GetMapping("/api/settings")
    public String settings() {
        return generalService.settings();
    }

    @PutMapping("/api/settings")
    public void postSettings(@RequestBody String body) throws ParseException {
        generalService.postSettings(body);
    }

    @PostMapping("/api/image")
    public String saveImage( @RequestPart("image") MultipartFile file, HttpServletRequest request) {
        return generalService.saveImage(file,request);
    }
}
