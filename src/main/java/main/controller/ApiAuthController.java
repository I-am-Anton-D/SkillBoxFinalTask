package main.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import main.model.CaptchaCode;
import main.model.CaptchaCodesRepository;
import main.model.PostVotesRepository;
import main.model.PostsRepository;
import main.model.User;
import main.model.UsersRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    final static String WRONG_PHOTO_SIZE = "Фото слишком большое, нужно не более 5 Мб";
    final static int AVATAR_SIZE = 36;

    @Value("${server.email}")
    private String serverEmail;
    @Value("${server.email.password}")
    private String serverEmailPassword;
    @Value("${server.root}")
    private String serverRoot;
    @Value("${auth.captcha.liveTime}")
    private String captchaLiveTime;
    @Value("${server.upload.path}")
    private String uploadRootPath;
    private JSONObject response, request = null;
    private final JSONParser parser = new JSONParser();
    public static Map<String, Integer> sessions = new HashMap<>();

    @Autowired private CaptchaCodesRepository captchaCodesRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private PostVotesRepository postVotesRepository;

    @PostMapping("/api/profile/my")
    public String editProfile(HttpServletRequest httpServletRequest)
        throws ParseException, IOException, ServletException {
        //TODO Remove on real server
        //String uploadRootPath = request.getServletContext().getRealPath("upload");
        boolean multipart = false;
        String mail = "", name = "", password = null, requestBody = null;
        Integer removePhoto = null;
        long size = -1;
        Part partPhoto = null;

        if (!checkLogin(httpServletRequest.getSession())) {
            return null;
        }

        User user = usersRepository.findById(sessions.get(httpServletRequest.getSession().getId())).get();
        try{
            requestBody = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            multipart = true;
        }
        if (multipart) {
            List<Part> parts = new ArrayList<>(httpServletRequest.getParts());
            for (Part p: parts) {
                if (p.getName().equals("name")) {
                    name = new String(p.getInputStream().readAllBytes());
                }
                if (p.getName().equals("email")) {
                    mail = new String(p.getInputStream().readAllBytes());
                }
                if (p.getName().equals("password")) {
                    password = new String(p.getInputStream().readAllBytes());
                }
                if (p.getName().equals("removePhoto")) {
                    removePhoto = Integer.parseInt(new String(p.getInputStream().readAllBytes()));
                }
                if (p.getName().equals("photo")) {
                    size = p.getSize();
                    if (size != 0 && size < 5242880) {
                        partPhoto = p;
                    }
                }
            }
        } else {
            response = new JSONObject();
            request = (JSONObject) parser.parse(requestBody);
            mail = request.get("email")!=null ? (String) request.get("email") :"";
            name =  request.get("name")!=null ? (String) request.get("name") : "";
            password = request.get("password")!=null ? (String) request.get("password") : null;
            removePhoto = request.get("removePhoto")!=null ? (int)((long) request.get("removePhoto")) : null;
        }

        JSONObject errors = new JSONObject();
        if (!mail.equals(user.getEmail())) {
            if (checkFreeMail(mail) && !mail.equals("")) {
                user.setEmail(mail);
            } else {
                errors.put("email", WRONG_MAIL);
            }
        }

        if (!name.equals(user.getName())) {
            if (name.length() == 0) {
                errors.put("name", WRONG_NAME);
            } else {
                user.setName(name);
            }
        }
        if (size==0 || size>5242880) {
            errors.put("photo", WRONG_PHOTO_SIZE);
        }

        if (password!=null) {
            if (password.length() < 6) {
                errors.put("password", WRONG_PASSWORD);
            } else {
                user.setPassword(Captcha.getMD5(password));
            }
        }

        if (errors.size()==0) {
            if (removePhoto!= null && removePhoto.equals(1)) {
                user.setPhoto(null);
            }
            if (partPhoto!=null) {
                user.setPhoto(saveAvatar(partPhoto));
            }
            usersRepository.save(user);
            response.put("result", true);
        } else {
            response.put("result", false);
            response.put("errors", errors);
        }
        return response.toJSONString();
    }

    @GetMapping("/login/change-password/{hash}")
    public String changePassword(@PathVariable String hash) {
        response = new JSONObject();
        response.put("result", true);
        return response.toJSONString();
    }

    @PostMapping("/api/auth/password")
    public String validateRestoringPassword(@RequestBody String body) {
        response = new JSONObject();
        response.put("result", true);
        return response.toJSONString();
    }

    @PostMapping("/api/auth/restore")
    public String restorePassword(@RequestBody String body) throws ParseException, AddressException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        String mail = (String) request.get("email");
        if (!checkFreeMail(mail)) {
            String code = Captcha.getMD5(Captcha.generateCaptchaText());
            User user = usersRepository.getUserByEmail(mail);
            user.setCode(code);
            usersRepository.save(user);
            sentMail(code,mail);
            response.put("result", true);
        } else {
            response.put("result", false);
        }
        return response.toJSONString();
    }

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

    private String saveAvatar(Part p){
        String random = "qwertytyuiopokkhffgasvxcbcvhrtey";
        StringBuilder randomPart = new StringBuilder();
        for (int i = 0; i <5 ; i++) {
            int r = new Random().nextInt(random.length()-1);
            randomPart.append(random.charAt(r));
        }
        try {
            byte[] bytes = p.getInputStream().readAllBytes();
            BufferedOutputStream stream =
                new BufferedOutputStream(
                    new FileOutputStream(new File(uploadRootPath + randomPart + p.getSubmittedFileName())));
            stream.write(bytes);
            stream.close();
            Thumbnails.of(uploadRootPath + randomPart + p.getSubmittedFileName())
                .size(AVATAR_SIZE, AVATAR_SIZE)
                .toFile(uploadRootPath + randomPart + p.getSubmittedFileName());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "/upload/"+randomPart+p.getSubmittedFileName();
    }

    public boolean checkFreeMail(String mail) {
         return usersRepository.checkFreeMail(mail)==0;
    }

    public User checkLoginPassword(String mail, String password) {
        return usersRepository.checkMailAndPassword(mail, Captcha.getMD5(password));
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
            jsonUser.put("moderationCount", postsRepository.getCountOfPostsForModeration());
        } else {
            jsonUser.put("moderation", false);
            jsonUser.put("settings", false);
        }
        return jsonUser;
    }

    private void sentMail(String code, String mail) throws AddressException {
        mail = "antmhy@gmail.com"; //TODO Remove on real server with real users
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(serverEmail);
        mailSender.setPassword(serverEmailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.valueOf(new InternetAddress(serverEmail)));
        message.setTo(mail);
        message.setSubject("Password restore");
        message.setText("Hello, go to "+ serverRoot +"/login/change-password/"+code);
        mailSender.send(message);
    }

    private boolean checkLogin(HttpSession session) {
        return sessions.containsKey(session.getId());
    }
}
