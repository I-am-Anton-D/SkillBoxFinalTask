package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Entity of Captcha code
 */
@Entity
public class CaptchaCode {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * time of generation captcha
     */

    @NotNull
    private Date time;

    /**
     * text of captcha
     */

    @NotNull
    @Column(columnDefinition = "TINYTEXT")
    private String code;

    /**
     * hash(MD5) of text of captcha
     */

    @NotNull
    @Column(columnDefinition = "TINYTEXT")
    private String secretCode;

    /**
     * Default constructor without parameters for Spring
     */

    public CaptchaCode() {
    }

    /**
     * Constructor of Entity
     *
     * @param time       generation time
     * @param code       text
     * @param secretCode hash of text(MD5)
     */

    public CaptchaCode(@NotNull Date time, @NotNull String code,
        @NotNull String secretCode) {
        this.time = time;
        this.code = code;
        this.secretCode = secretCode;
    }

    /**
     * Standard getter
     *
     * @return time of generation captcha
     */

    public Date getTime() {
        return time;
    }

    /**
     * Standard getter
     *
     * @return text of captcha
     */

    public String getCode() {
        return code;
    }

    /**
     * Standard getter
     *
     * @return MD5 of captcha
     */

    public String getSecretCode() {
        return secretCode;
    }
}
