package main.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity for votes
 */
@Entity
@Table(name = "post_votes")
public class PostVote {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * id of user, who voting
     */

    @NotNull
    private int userId;

    /**
     * id of post, that was voting
     */

    @NotNull
    private int postId;

    /**
     * time of voting
     */

    @NotNull
    private Date time;

    /**
     * value of vote. 1 = like, -1 = dislike
     */

    @NotNull
    private byte value;

    /**
     * Empty constructor for Spring
     */

    public PostVote() {
    }

    /**
     * Constructor for creating entity
     */

    public PostVote(@NotNull int userId, @NotNull int postId,
        @NotNull Date time, @NotNull byte value) {
        this.userId = userId;
        this.postId = postId;
        this.time = time;
        this.value = value;
    }

    /**
     * Standard getter
     *
     * @return id of vote
     */

    public int getId() {
        return id;
    }

    /**
     * Standard getter
     *
     * @return id of user, who voting
     */

    public int getUserId() {
        return userId;
    }

    /**
     * Standard getter
     *
     * @return id of post, that was voting
     */

    public int getPostId() {
        return postId;
    }

    /**
     * Standard getter
     *
     * @return time of voting
     */

    public Date getTime() {
        return time;
    }

    /**
     * Standard getter
     *
     * @return vote value: 1 = like, -1 = dislike
     */

    public byte getValue() {
        return value;
    }
}
