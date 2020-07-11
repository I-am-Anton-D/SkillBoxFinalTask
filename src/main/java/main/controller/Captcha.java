package main.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import javax.imageio.ImageIO;
import main.model.CaptchaCode;
import main.model.CaptchaCodesRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.config.Task;

public class Captcha {
    final static String LETTERS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    final static String CAPTCHA_FONT = "Arial";
    final static int CAPTCHA_WIDTH = 90;
    final static int CAPTCHA_HEIGHT = 30;
    final static int CAPTCHA_FONT_HEIGHT = 20;


    public static String generateCaptchaText(int captchaLength) {
        StringBuilder captchaBuffer = new StringBuilder();
        Random random = new Random();

        while(captchaBuffer.length() < captchaLength) {
            int index = (int) (random.nextFloat() * LETTERS.length());
            captchaBuffer.append(LETTERS, index, index+1);
        }
        return captchaBuffer.toString();
    }

    public static void save(CaptchaCode captchaCode, CaptchaCodesRepository repository) {
        repository.save(captchaCode);
    }

    public static void deleteExpired(long captchaLiveTime, CaptchaCodesRepository repository) {
        Iterable<CaptchaCode> codes = repository.findAll();
        long now = new Date().getTime();
        codes.forEach(captchaCode -> {
            if (now-captchaCode.getTime().getTime()>captchaLiveTime) {
                repository.delete(captchaCode);
            }
        });
    }


    public static String generateCaptchaImage(String captchaText){
        BufferedImage bufferedImage = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.OPAQUE);
        Graphics graphics = bufferedImage.createGraphics();
        graphics.setFont(new Font(CAPTCHA_FONT, Font.BOLD, CAPTCHA_FONT_HEIGHT));
        graphics.setColor(new Color(169, 169, 169));
        graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
        graphics.setColor(new Color(255, 255, 255));
        graphics.drawString(captchaText, 10, 22);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i <3 ; i++) {
            graphics.drawOval(new Random().nextInt(CAPTCHA_WIDTH/2),new Random().nextInt(CAPTCHA_HEIGHT/2),new Random().nextInt(100),new Random().nextInt(100));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "data:image/png;base64, " + new String(Base64.encodeBase64(baos.toByteArray()), StandardCharsets.UTF_8);
    }

    public static String getMD5(String string) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(string.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        while(hashtext.length() < 32 ){
            hashtext = "0"+hashtext;
        }
        return hashtext;
    }
}
