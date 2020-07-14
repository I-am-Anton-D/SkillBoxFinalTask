package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    @Column(name = "is_moderator")
    private byte moderator;
    @NotNull
    private Date regTime;
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private String code;
    @Column(columnDefinition = "text")
    private String photo;

    public User() {}
    public User(@NotNull Date regTime, @NotNull String name,
        @NotNull String email, @NotNull String password) {
        this.regTime = regTime;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getModerator() {
        return moderator;
    }

    public void setModerator(byte moderator) {
        this.moderator = moderator;
    }

    public Date getRegTime() {
        return regTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isModerator() {
        return getModerator() == 1;
    }
}
