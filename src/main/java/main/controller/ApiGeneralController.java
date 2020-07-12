package main.controller;

import java.util.Map.Entry;
import main.model.GlobalSettings;
import main.model.GlobalSettingsRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiGeneralController {
    final static String MULTIUSER_MODE = "Многопользовательский режим";
    final static String POST_PREMODERATION = "Премодерация постов";
    final static String STATISTICS_IS_PUBLIC = "Показывать всем статистику блога";


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

    private JSONObject response, request = null;
    private JSONParser parser = new JSONParser();
    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

    @GetMapping("/api/init")
    public String init() {
        response = new JSONObject();
        response.put("title", title);
        response.put("subtitle", subtitle);
        response.put("phone", phone);
        response.put("email", mail);
        response.put("copyright", copyright);
        response.put("copyrightFrom", copyrightFrom);
        return response.toJSONString();
    }

    @GetMapping("/api/settings")
    public String settings() {
        response = new JSONObject();
        Iterable<GlobalSettings> settings = globalSettingsRepository.findAll();
        settings.forEach(globalSettings -> {
            response.put(globalSettings.getCode(), globalSettings.getValue());
        });
        return response.toJSONString();
    }

    @PutMapping("/api/settings")
    public void postSettings(@RequestBody String body) throws ParseException {
        request = (JSONObject) parser.parse(body);
        Iterable<GlobalSettings> settings = globalSettingsRepository.findAll();
        for (Object key : request.keySet()) {
            String code = (String) key;
            GlobalSettings globalSettings = new GlobalSettings();
            globalSettings.setCode(code);
            switch (code) {
                case "MULTIUSER_MODE": {
                    globalSettings.setName(MULTIUSER_MODE);
                    break;
                }
                case "POST_PREMODERATION": {
                    globalSettings.setName(POST_PREMODERATION);
                    break;
                }
                case "STATISTICS_IS_PUBLIC": {
                    globalSettings.setName(STATISTICS_IS_PUBLIC);
                    break;
                }
            }
            globalSettings.setValue((boolean) request.get(code));
            for (GlobalSettings gs : settings) {
                if (gs.getCode().equals(code)) {
                    globalSettingsRepository.delete(gs);
                    break;
                }
            }
            globalSettingsRepository.save(globalSettings);
        }

    }
}
