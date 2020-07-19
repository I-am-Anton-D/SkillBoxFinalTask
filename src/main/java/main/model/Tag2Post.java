package main.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Entity for connection posts and tags
 */
@Entity
public class Tag2Post {

    /**
     * unique id in DB
     */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /**
     * id of post
     */

    @NotNull
    private int postId;

    /**
     * id of tag
     */

    @NotNull
    private int tagId;

    /**
     * empty constructor for Spring
     */

    public Tag2Post() {
    }

    /**
     * constructor for creating entity
     */

    public Tag2Post(@NotNull int postId, @NotNull int tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }

    /**
     * Standard getter
     *
     * @return id of record
     */

    public int getId() {
        return id;
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
     * @return id of tag
     */

    public int getTagId() {
        return tagId;
    }
}
