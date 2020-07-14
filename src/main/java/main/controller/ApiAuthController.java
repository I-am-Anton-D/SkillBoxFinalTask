package main.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;
import main.model.CaptchaCode;
import main.model.CaptchaCodesRepository;
import main.model.PostsRepository;
import main.model.User;
import main.model.UsersRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import static main.model.ModerationStatus.*;

@RestController
public class ApiAuthController {
    final static String WRONG_CAPTCHA = "Код с картинки введён неверно";
    final static String WRONG_NAME = "Имя указано неверно";
    final static String WRONG_PASSWORD = "Пароль короче 6-ти символов";
    final static String WRONG_MAIL = "Этот e-mail уже зарегистрирован";

    @Value("${auth.captcha.liveTime}")
    private String captchaLiveTime;
    private JSONObject response, request = null;
    private final JSONParser parser = new JSONParser();
    public static Map<String, Integer> sessions = new HashMap<>();

    @Autowired private CaptchaCodesRepository captchaCodesRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private PostsRepository postsRepository;

    @PostMapping("/api/auth/login")
    public String login(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        User user = checkLoginPassword((String) request.get("e_mail"), (String) request.get("password"));
        if (user!=null) {
            sessions.put(httpRequest.getSession().getId(), user.getId());
            return check(httpRequest);
        } else {
            response.put("result",false);
            return response.toJSONString();
        }
    }

    @GetMapping("/api/auth/check")
    public String check(HttpServletRequest httpRequest) {
        response = new JSONObject();
        if (sessions.containsKey(httpRequest.getSession().getId())) {
            User user = usersRepository.findById(sessions.get(httpRequest.getSession().getId())).get();
            response.put("result", true);
            response.put("user", transformUserToJson(user));
        } else {
            response.put("result", false);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/auth/logout")
    public String logout(HttpServletRequest httpRequest){
        sessions.remove(httpRequest.getSession().getId());
        response = new JSONObject();
        response.put("result", true);
        return response.toJSONString();
    }

    @PostMapping("/api/auth/register")
    public String register(@RequestBody String body) throws ParseException {
        request = (JSONObject) parser.parse(body);
        String mail = (String) request.get("e_mail");
        String password = (String) request.get("password");
        String name = (String) request.get("name");
        String captcha = (String) request.get("captcha");
        String secret = (String) request.get("captcha_secret");
        response = new JSONObject();
        JSONObject errors = new JSONObject();
        if (!Captcha.validate(captcha,secret,captchaCodesRepository)) {
            errors.put("captcha",WRONG_CAPTCHA);
        }
        if (name.length() == 0) {
            errors.put("name", WRONG_NAME );
        }
        if (password.length()<6) {
            errors.put("password",WRONG_PASSWORD);
        }
        if (!checkFreeMail(mail)) {
            errors.put("email", WRONG_MAIL);
        }
        if (errors.size()!=0) {
            response.put("result",false);
            response.put("errors",errors);
        }
        else {
            usersRepository.save(new User(new Date(), name, mail, Captcha.getMD5(password)));
            response.put("result",true);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/auth/captcha")
    public String captcha() throws IOException {
        String captchaText = Captcha.generateCaptchaText();
        String captchaImage = Captcha.generateCaptchaImage(captchaText);
        String secret = Captcha.getMD5(captchaText);

        Captcha.save(new CaptchaCode(new Date(), captchaText, secret), captchaCodesRepository);
        Captcha.deleteExpired(Long.parseLong(captchaLiveTime), captchaCodesRepository);

        response = new JSONObject();
        response.put("secret", secret);
        response.put("image", captchaImage);
        return response.toJSONString();
    }

    public boolean checkFreeMail(String mail) {
        for (User user:usersRepository.findAll()) {
            if (user.getEmail().equals(mail)) return false;
        }
        return true;
    }

    public User checkLoginPassword(String mail, String password) {
        for (User user: usersRepository.findAll()) {
           if (user.getEmail().equals(mail) && user.getPassword().equals(Captcha.getMD5(password))) {
               return user;
           }
        }
        return null;
    }

    public JSONObject transformUserToJson(User user) {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", user.getId());
        jsonUser.put("name", user.getName());
        jsonUser.put("photo", user.getPhoto());
        jsonUser.put("email", user.getEmail());
        if (user.isModerator()) {
            jsonUser.put("moderation", true);
            jsonUser.put("settings", true);
            jsonUser.put("moderationCount", calculateModerationCount());
        } else {
            jsonUser.put("moderation", false);
            jsonUser.put("settings", false);
        }
        return jsonUser;
    }

    private int calculateModerationCount() {
        return (int) StreamSupport.stream(postsRepository.findAll().spliterator(),false)
            .filter(p->p.getIsActive()==1 && p.getModerationStatus()==NEW).count();
    }
}
