package main.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import main.model.GlobalSettings;
import main.repositories.GlobalSettingsRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GeneralService {
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
    @Value("${server.upload.path}")
    private String uploadRootPath;
    private JSONObject response, request = null;
    private JSONParser parser = new JSONParser();
    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;

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

    public String settings() {
        response = new JSONObject();
        globalSettingsRepository.findAll()
            .forEach(globalSettings -> response.put(globalSettings.getCode(), globalSettings.getValue()));
        return response.toJSONString();
    }

    public void postSettings(String body) throws ParseException {
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

    public String saveImage(MultipartFile file, HttpServletRequest request) {
        //TODO Take next string on real server;
        // String uploadRootPath = request.getServletContext().getRealPath("upload");
        String random = "qwertytyuiopokkhffgasvxcbcvhrtey";
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i <5 ; i++) {
            int r = new Random().nextInt(random.length()-1);
            randomPart.append(random.charAt(r));
        }
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new File(uploadRootPath + randomPart+file.getOriginalFilename())));
                stream.write(bytes);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "/upload/"+randomPart+file.getOriginalFilename();
    }
}
