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
@Table(name = "post_comments")
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private Integer parentId;
    @NotNull
    private int postId;
    @NotNull
    private int userId;
    @NotNull
    private Date time;
    @NotNull
    @Column(columnDefinition = "text")
    private String text;

    public PostComment() {}
    public PostComment(Integer parentId, @NotNull int postId, @NotNull int userId,
        @NotNull Date time, @NotNull String text) {
        this.parentId = parentId;
        this.postId = postId;
        this.userId = userId;
        this.time = time;
        this.text = text;
    }

    public int getId() {return id;}
    public Integer getParentId() {
        return parentId;
    }
    public int getPostId() {
        return postId;
    }
    public int getUserId() {
        return userId;
    }
    public Date getTime() {
        return time;
    }
    public String getText() {
        return text;
    }
}
