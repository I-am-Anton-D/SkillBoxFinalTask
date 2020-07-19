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
 * Entity for comments
 */

@Entity
@Table(name = "post_comments")
public class PostComment {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * parent for this comment, can be null
     */

    private Integer parentId;

    /**
     * id of post, that it's comment have
     */

    @NotNull
    private int postId;

    /**
     * user id, who make the comment
     */

    @NotNull
    private int userId;

    /**
     * time of creation comment
     */

    @NotNull
    private Date time;

    /**
     * text of comment
     */

    @NotNull
    @Column(columnDefinition = "text")
    private String text;

    /**
     * Empty constructor for Spring
     */

    public PostComment() {
    }

    /**
     * Constructor for creating entity
     *
     * @param parentId comment parent
     * @param postId   id of post
     * @param userId   user id of comment
     * @param time     of creation
     * @param text     of o comment
     */
    public PostComment(Integer parentId, @NotNull int postId, @NotNull int userId,
        @NotNull Date time, @NotNull String text) {
        this.parentId = parentId;
        this.postId = postId;
        this.userId = userId;
        this.time = time;
        this.text = text;
    }

    /**
     * Standard getter
     *
     * @return id of comment
     */

    public int getId() {
        return id;
    }

    /**
     * Standard getter
     *
     * @return of parent comment
     */

    public Integer getParentId() {
        return parentId;
    }

    /**
     * Standard getter
     *
     * @return id of post
     */

    public int getPostId() {
        return postId;
    }

    /**
     * Standard getter
     *
     * @return id of user, who make the post
     */

    public int getUserId() {
        return userId;
    }

    /**
     * Standard getter
     *
     * @return creation time
     */

    public Date getTime() {
        return time;
    }

    /**
     * Standard getter
     *
     * @return text of comment
     */

    public String getText() {
        return text;
    }
}
