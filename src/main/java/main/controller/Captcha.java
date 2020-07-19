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
import main.repositories.CaptchaCodesRepository;
import org.apache.tomcat.util.codec.binary.Base64;

/**
 * Class for generation captcha image
 */

public class Captcha {

    /**
     * Random string for captcha
     */

    final static String LETTERS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Font for captcha, width, height, font size for captcha image
     */

    final static String CAPTCHA_FONT = "Arial";
    final static int CAPTCHA_WIDTH = 90;
    final static int CAPTCHA_HEIGHT = 30;
    final static int CAPTCHA_FONT_HEIGHT = 20;

    /**
     * Count of letters in captcha image
     */

    final static int CAPTCHA_LETTERS_COUNT = 5;

    /**
     * Generating random captcha string
     *
     * @return string with random letters
     */

    public static String generateCaptchaText() {
        StringBuilder captchaBuffer = new StringBuilder();
        Random random = new Random();

        while (captchaBuffer.length() < CAPTCHA_LETTERS_COUNT) {
            int index = (int) (random.nextFloat() * LETTERS.length());
            captchaBuffer.append(LETTERS, index, index + 1);
        }
        return captchaBuffer.toString();
    }

    /**
     * Validate input captcha string with original
     *
     * @param code       input string
     * @param secret     original string
     * @param repository for validate with DB
     * @return true - if input == original, false otherwise
     */

    public static boolean validate(String code, String secret, CaptchaCodesRepository repository) {
        if (code.length() < CAPTCHA_LETTERS_COUNT) {
            return false;
        }

        Iterable<CaptchaCode> codes = repository.findAll();
        for (CaptchaCode captchaCode : codes) {
            if (captchaCode.getSecretCode().equals(secret) && captchaCode.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saving captcha text in DB
     *
     * @param captchaCode original text of captcha
     * @param repository  for saving string in DB
     */

    public static void save(CaptchaCode captchaCode, CaptchaCodesRepository repository) {
        repository.save(captchaCode);
    }

    /**
     * Deleting expiring captcha codes in DB.
     *
     * @param captchaLiveTime time(in ms) to storage captcha in DB. It's parameter set in application.yml
     * @param repository      for deleting records in DB
     */

    public static void deleteExpired(long captchaLiveTime, CaptchaCodesRepository repository) {
        Iterable<CaptchaCode> codes = repository.findAll();
        long now = new Date().getTime();

        codes.forEach(captchaCode -> {
            if (now - captchaCode.getTime().getTime() > captchaLiveTime) {
                repository.delete(captchaCode);
            }
        });
    }

    /**
     * Generatin captcha image. Using some random ovals for complicating
     *
     * @param captchaText original captcha text
     * @return image in base65 format
     */

    public static String generateCaptchaImage(String captchaText) {
        BufferedImage bufferedImage = new BufferedImage(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, BufferedImage.OPAQUE);
        Graphics graphics = bufferedImage.createGraphics();
        graphics.setFont(new Font(CAPTCHA_FONT, Font.BOLD, CAPTCHA_FONT_HEIGHT));
        graphics.setColor(new Color(169, 169, 169));
        graphics.fillRect(0, 0, CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
        graphics.setColor(new Color(255, 255, 255));
        graphics.drawString(captchaText, 10, 22);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < 3; i++) {
            graphics.drawOval(new Random().nextInt(CAPTCHA_WIDTH / 2), new Random().nextInt(CAPTCHA_HEIGHT / 2),
                new Random().nextInt(100), new Random().nextInt(100));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "data:image/png;base64, " + new String(Base64.encodeBase64(baos.toByteArray()),
            StandardCharsets.UTF_8);
    }

    /**
     * Gettin MD5 for text of captcha. Also use for encryption user password and link for restore password
     *
     * @param string string to encryption
     * @return hash in MD5
     */

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
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString(16);

        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}
