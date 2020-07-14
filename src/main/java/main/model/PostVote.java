package main.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "post_votes")
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotNull
    private int userId;
    @NotNull
    private int postId;
    @NotNull
    private Date time;
    @NotNull
    private byte value;

    public PostVote() {}
    public PostVote(@NotNull int userId, @NotNull int postId,
        @NotNull Date time, @NotNull byte value) {
        this.userId = userId;
        this.postId = postId;
        this.time = time;
        this.value = value;
    }

    public int getId() {
        return id;
    }
    public int getUserId() {
        return userId;
    }
    public int getPostId() {
        return postId;
    }
    public Date getTime() {
        return time;
    }
    public byte getValue() {
        return value;
    }
}
