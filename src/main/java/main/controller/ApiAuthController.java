package main.controller;

import java.io.IOException;
import java.util.Date;
import main.model.CaptchaCode;
import main.model.CaptchaCodesRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiAuthController {
    @Value("${auth.captcha.liveTime}")
    private String captchaLiveTime;

    private JSONObject response;
    @Autowired
    private CaptchaCodesRepository captchaCodesRepository;

    @GetMapping("/api/auth/check")
    public String check() {
        response = new JSONObject();
        response.put("result","false");
        return response.toJSONString();
    }

    @GetMapping("/api/auth/captcha")
    public String captcha() throws IOException {
        String captchaText = Captcha.generateCaptchaText(5);
        String captchaImage = Captcha.generateCaptchaImage(captchaText);
        String secret = Captcha.getMD5(captchaText);

        Captcha.save(new CaptchaCode(new Date(),captchaText,secret), captchaCodesRepository);
        Captcha.deleteExpired(Long.parseLong(captchaLiveTime), captchaCodesRepository);

        response = new JSONObject();
        response.put("secret",secret);
        response.put("image",captchaImage);

        return response.toJSONString();
    }
}
