package main.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity of post
 */
@Entity
@Table(name = "posts")
public class Post {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * visible of post. if active = 1 -> post visible, else not
     */

    @NotNull
    @Column(name = "is_active")
    private byte active;

    /**
     * Status of moderation for post
     */

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('NEW','ACCEPTED','DECLINED')")
    private ModerationStatus moderationStatus = ModerationStatus.NEW;

    /**
     * id of moderator
     */

    private int moderatorId;

    /**
     * id of user, who make the post
     */

    @NotNull
    private int userId;

    /**
     * time of creating the post
     */

    @NotNull
    private Date time;

    /**
     * title of post
     */

    @NotNull
    private String title;

    /**
     * text of post
     */

    @NotNull
    @Column(columnDefinition = "text")
    private String text;

    /**
     * count of views
     */

    @NotNull
    private int viewCount;

    /**
     * Standard getter
     *
     * @return id of post
     */

    public int getId() {
        return id;
    }

    /**
     * Standard setter set the id of post
     */

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Standard getter
     *
     * @return visibility of post
     */

    public byte getActive() {
        return active;
    }

    /**
     * Standard setter set visibility of post
     */

    public void setActive(byte active) {
        this.active = active;
    }

    /**
     * Standard getter
     *
     * @return status of moderation
     */

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    /**
     * Standard setter set moderation status for post
     */

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    /**
     * Standard getter
     *
     * @return id of moderator
     */


    public int getModeratorId() {
        return moderatorId;
    }

    /**
     * Standard setter set moderator id for post
     */

    public void setModeratorId(int moderatorId) {
        this.moderatorId = moderatorId;
    }


    /**
     * Standard getter
     *
     * @return user id, who make the post
     */

    public int getUserId() {
        return userId;
    }

    /**
     * Standard setter set moderator id for post
     */

    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Standard getter
     *
     * @return time of creation for post
     */

    public Date getTime() {
        return time;
    }

    /**
     * Standard setter set creation time for post
     */


    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Standard getter
     *
     * @return title of post
     */

    public String getTitle() {
        return title;
    }

    /**
     * Standard setter set title for post
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Standard getter
     *
     * @return text of post
     */

    public String getText() {
        return text;
    }

    /**
     * Standard setter set text for post
     */

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Standard getter
     *
     * @return count of views for post
     */

    public int getViewCount() {
        return viewCount;
    }

    /**
     * Standard setter set coint of views for post
     */

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
