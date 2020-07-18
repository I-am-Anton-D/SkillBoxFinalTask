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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import main.model.CaptchaCode;
import main.repositories.CaptchaCodesRepository;
import main.repositories.PostVotesRepository;
import main.repositories.PostsRepository;
import main.model.User;
import main.repositories.UsersRepository;
import main.service.AuthService;
import main.service.PostService;
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

@RestController
public class ApiAuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/api/profile/my")
    public String editProfile(HttpServletRequest httpServletRequest)
        throws ParseException, IOException, ServletException {
        return authService.editProfile(httpServletRequest);
    }

    @GetMapping("/login/change-password/{hash}")
    public String changePassword(@PathVariable String hash) {
        return authService.changePassword(hash);
    }

    @PostMapping("/api/auth/password")
    public String validateRestoringPassword(@RequestBody String body) {
        return authService.validateRestoringPassword(body);
    }

    @PostMapping("/api/auth/restore")
    public String restorePassword(@RequestBody String body) throws ParseException, AddressException {
        return authService.restorePassword(body);
    }

    @PostMapping("/api/auth/login")
    public String login(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return authService.login(body,httpRequest);
    }

    @GetMapping("/api/auth/check")
    public String check(HttpServletRequest httpRequest) {
        return authService.check(httpRequest);
    }

    @GetMapping("/api/auth/logout")
    public String logout(HttpServletRequest httpRequest){
        return authService.logout(httpRequest);
    }

    @PostMapping("/api/auth/register")
    public String register(@RequestBody String body) throws ParseException {
        return authService.register(body);
    }

    @GetMapping("/api/auth/captcha")
    public String captcha() throws IOException {
        return authService.captcha();
    }
}
