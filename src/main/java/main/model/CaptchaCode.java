package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class CaptchaCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    private Date time;
    @NotNull
    @Column(columnDefinition = "TINYTEXT")
    private String code;
    @NotNull
    @Column(columnDefinition = "TINYTEXT")
    private String secretCode;

    public CaptchaCode() {}
    public CaptchaCode(@NotNull Date time, @NotNull String code,
        @NotNull String secretCode) {
        this.time = time;
        this.code = code;
        this.secretCode = secretCode;
    }

    public Date getTime() {
        return time;
    }
    public String getCode() {
        return code;
    }
    public String getSecretCode() {
        return secretCode;
    }
}
