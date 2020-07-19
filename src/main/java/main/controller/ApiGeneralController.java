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

/**
 * Rest Controller for general settings and init
 */

@RestController
public class ApiGeneralController {

    /**
     * Service for for general settings and init
     */

    @Autowired
    private GeneralService generalService;

    /**
     * Initialization of application (inserting title, subtilte and etc)
     *
     * @return response in JSON, @see GeneralService.init();
     */

    @GetMapping("/api/init")
    public String init() {
        return generalService.init();
    }

    /**
     * Getting general settings
     *
     * @return response in JSON, @see GeneralService.settings()
     */
    @GetMapping("/api/settings")
    public String settings() {
        return generalService.settings();
    }

    /**
     * Saving new settings
     *
     * @param body request in JSON
     * @throws ParseException - if can not parse Response Body to Json
     */

    @PutMapping("/api/settings")
    public void postSettings(@RequestBody String body) throws ParseException {
        generalService.postSettings(body);
    }

    /**
     * Saving file (image) from frontend
     *
     * @param file    file from frontend
     * @param request using for detecting user
     * @return source to downloading file
     */

    @PostMapping("/api/image")
    public String saveImage(@RequestPart("image") MultipartFile file, HttpServletRequest request) {
        return generalService.saveImage(file, request);
    }
}
