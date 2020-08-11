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

/**
 * Rest Controller for authorization and working with profile of user
 */

@RestController
public class ApiAuthController {

    /**
     * Service for authorization
     */

    @Autowired
    AuthService authService;

    /**
     * Edit profile of user
     *
     * @param httpServletRequest - using for detecting user
     * @return JSON response to front end, @see AuthService.editProfile()
     * @throws ParseException   - if can not parse Response Body to Json
     * @throws IOException      - if can not write the image (avatar)
     * @throws ServletException - using for multipart request detection
     */

    @PostMapping("/api/profile/my")
    public String editProfile(HttpServletRequest httpServletRequest)
        throws ParseException, IOException, ServletException {
        return authService.editProfile(httpServletRequest);
    }

    /**
     * Validating restoring password
     *
     * @param body - request body in Json
     * @return JSON response @see AuthService.validateRestoringPassword()
     */

    @PostMapping("/api/auth/password")
    public String validateRestoringPassword(@RequestBody String body) throws ParseException {
        return authService.validateRestoringPassword(body);
    }

    /**
     * Restoring password of user
     *
     * @param body request body in JSON
     * @return JSON response @see AuthService.restorePassword()
     * @throws ParseException   if can not parse Response Body to Json
     * @throws AddressException if can not sent e-mail
     */

    @PostMapping("/api/auth/restore")
    public String restorePassword(@RequestBody String body,  HttpServletRequest request) throws ParseException, AddressException {
        return authService.restorePassword(body, request.getLocalName());
    }

    /**
     * Login the user
     *
     * @param body        request body in JSON
     * @param httpRequest using for detecting user
     * @return JSON response @see AuthService.login()
     * @throws ParseException if can not parse Response Body tp Json
     */

    @PostMapping("/api/auth/login")
    public String login(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return authService.login(body, httpRequest);
    }

    /**
     * Checking authorization of user
     *
     * @param httpRequest using for detecting user
     * @return JSON response @see AuthService.check()
     */

    @GetMapping("/api/auth/check")
    public String check(HttpServletRequest httpRequest) {
        return authService.check(httpRequest);
    }

    /**
     * Logout of user
     *
     * @param httpRequest using for detecting user
     * @return JSON response @see AuthService.logout()
     */

    @GetMapping("/api/auth/logout")
    public String logout(HttpServletRequest httpRequest) {
        return authService.logout(httpRequest);
    }

    /**
     * Registration of user
     *
     * @param body using for detecting user
     * @return JSON response @see AuthService.register()
     * @throws ParseException if can not parse Response Body tp Json
     */

    @PostMapping("/api/auth/register")
    public String register(@RequestBody String body) throws ParseException {
        return authService.register(body);
    }

    /**
     * Generation of captcha
     *
     * @return JSON response @see AuthService.captcha()
     */

    @GetMapping("/api/auth/captcha")
    public String captcha() {
        return authService.captcha();
    }
}
