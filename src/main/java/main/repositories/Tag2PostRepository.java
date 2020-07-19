package main.repositories;

import main.model.Tag2Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for working with connections between posts and tags
 */

public interface Tag2PostRepository extends CrudRepository<Tag2Post, Integer> {

    /**
     * Getting frequency of using for specific tag in all visible posts
     *
     * @param tagId id of tag
     * @return frequency of using
     */

    @Query(value = "SELECT COUNT(*) FROM tag2post t JOIN (SELECT id AS postID FROM "
        + "posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW()) ps ON t.post_id = ps.postID WHERE t.tag_id=:tagId", nativeQuery = true)
    int getFrequencyOfTag(@Param("tagId") int tagId);

}