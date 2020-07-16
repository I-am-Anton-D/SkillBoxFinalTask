package main.model;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PostsRepository extends CrudRepository<Post, Integer> {

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY posts.time LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getVisiblePosts(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int countOfVisiblePosts();

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() ORDER BY posts.time", nativeQuery = true)
    List<Post> getAllVisiblePosts();

    @Query(value = "SELECT * FROM posts JOIN tag2post ON tag2post.post_id = posts.id "
        + "WHERE tag_id = (SELECT id FROM tags WHERE tags.name = :tagName) AND posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() "
        + "ORDER BY posts.time LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsByTag(@Param("tagName") String tagName, @Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM posts JOIN tag2post ON tag2post.post_id = posts.id "
        + "WHERE tag_id = (SELECT id FROM tags WHERE tags.name = :tagName) AND posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW()", nativeQuery = true)
        int getPostsByTagCount(@Param("tagName") String tagName);
}
