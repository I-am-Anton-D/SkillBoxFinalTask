package main.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface Tag2PostRepository extends CrudRepository<Tag2Post, Integer> {

    @Query(value = "SELECT COUNT(*) FROM tag2post t JOIN (SELECT id AS postID FROM "
        + "posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW()) ps ON t.post_id = ps.postID WHERE t.tag_id=:tagId", nativeQuery = true)
    int getFrequencyOfTag(@Param("tagId") int tagId);

    @Query(value = "DELETE * FROM tag2post WHERE post_id = :postId", nativeQuery = true)
    void deleteTagsOnPost(@Param("postId") int postId);

}