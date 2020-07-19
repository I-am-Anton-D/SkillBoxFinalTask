package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity for users
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * moderator = 1, else 0
     */

    @NotNull
    @Column(name = "is_moderator")
    private byte moderator;

    /**
     * registration time
     */

    @NotNull
    private Date regTime;

    /**
     * name of user
     */

    @NotNull
    private String name;

    /**
     * email of user
     */

    @NotNull
    private String email;

    /**
     * password(MD5) of user
     */

    @NotNull
    private String password;

    /**
     * code for restoring password
     */

    private String code;

    /**
     * link to avatar of user
     */

    @Column(columnDefinition = "text")
    private String photo;

    /**
     * empty constructor for Spring
     */

    public User() {
    }

    /**
     * constructor for creating entity
     */

    public User(@NotNull Date regTime, @NotNull String name,
        @NotNull String email, @NotNull String password) {
        this.regTime = regTime;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Standard getter
     *
     * @return id of user
     */

    public int getId() {
        return id;
    }

    /**
     * Standard setter set id for user
     */

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Standard getter
     *
     * @return 1 - moderator, otherwise = 0
     */

    public byte getModerator() {
        return moderator;
    }

    /**
     * Standard setter set 1 for moderators
     */

    public void setModerator(byte moderator) {
        this.moderator = moderator;
    }

    /**
     * Standard getter
     *
     * @return registration time
     */

    public Date getRegTime() {
        return regTime;
    }

    /**
     * Standard getter
     *
     * @return name of user
     */

    public String getName() {
        return name;
    }

    /**
     * Standard setter set the name for user
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Standard getter
     *
     * @return email of user
     */

    public String getEmail() {
        return email;
    }

    /**
     * Standard setter set the email for user
     */

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Standard getter
     *
     * @return password(MD5) of user
     */

    public String getPassword() {
        return password;
    }

    /**
     * Standard setter set the password(MD5) for user
     */

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Standard getter
     *
     * @return hash for restoring password
     */


    public String getCode() {
        return code;
    }

    /**
     * Standard setter set the hash for restoring the password
     */


    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Standard getter
     *
     * @return lint for avatar
     */


    public String getPhoto() {
        return photo;
    }

    /**
     * Standard setter set the link for avator
     */


    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * Standard getter
     *
     * @return true if moderetor = 1
     */

    public boolean isModerator() {
        return getModerator() == 1;
    }
}
