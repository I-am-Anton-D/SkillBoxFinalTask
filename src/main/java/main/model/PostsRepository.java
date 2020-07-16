package main.model;

import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder.In;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PostsRepository extends CrudRepository<Post, Integer> {

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY posts.time DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getVisiblePosts(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY posts.time LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getVisiblePostsOrderDesc(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY (SELECT count(*) FROM post_comments WHERE post_id=posts.id) DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPopularPosts(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY (SELECT count(*) FROM post_votes WHERE post_id = posts.id and value=1) DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getBestPosts(@Param("offset") int offset, @Param("limit") int limit);

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

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND posts.text like :query OR  posts.title like :query ORDER BY posts.time DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsByQuery(@Param("query") String query, @Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND posts.text like :query OR posts.title like :query", nativeQuery = true)
    int getCountOfPostsByQuery(@Param("query") String query);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND DATE(posts.time) = :date ORDER BY posts.time DESC "
        + "LIMIT :limit OFFSET :offset" , nativeQuery = true)

    List<Post> getPostsByDate(@Param("date") String date, @Param("offset") int offset, @Param("limit") int limit);
    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND DATE(posts.time) = :date" , nativeQuery = true)
    int getCountOfPostsByDate(@Param("date") String date);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = :status "
        + "AND time<=NOW() AND (posts.moderator_id = 0 OR posts.moderator_id = :userId) ORDER BY posts.time "
        + "DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsForModeration(@Param("status") String status, @Param("userId") int userId,
        @Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = :status "
        + "AND time<=NOW() AND (posts.moderator_id = 0 OR posts.moderator_id = :userId)", nativeQuery = true)
    int getCountOfPostsForModeration(@Param("status") String status, @Param("userId") int userId);

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = :active "
        + "AND posts.moderation_status = :status AND time<=NOW() AND posts.user_id = :userId ORDER BY posts.time "
        + "DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsOfUser(@Param("status") String status, @Param("userId") int userId, @Param("active") byte active,
        @Param("offset") int offset, @Param("limit") int limit);
    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = :active "
        + "AND posts.moderation_status = :status AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getCountOfPostsOfUser(@Param("status") String status, @Param("userId") int userId, @Param("active") byte active);

    @Query(value = "SELECT DISTINCT year(time) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW();", nativeQuery = true)
    List<Integer> getYearsOfPost();

    @Query(value = "SELECT DISTINCT date(time) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() ORDER BY time;", nativeQuery = true)
    List<Date> getPostDates();

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() GROUP BY date(time) ORDER BY time", nativeQuery = true)
    List<Integer> getCountOfPostByDate();

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getCountOfUserPosts(@Param("userId") int userId);

    @Query(value = "SELECT sum(view_count) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getSumViewCountOfUserPosts(@Param("userId") int userId);

    @Query(value = "SELECT time FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId order by time limit 1", nativeQuery = true)
    Date getFirstPublicationDateOfUserPosts(@Param("userId") int userId);

    @Query(value = "SELECT count(*) FROM posts JOIN post_votes "
        + "ON post_votes.post_id = posts.id and post_votes.value = :value WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND posts.time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getVotesCountOfUser(@Param("userId") int userId, @Param("value") int value);

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int getCountOfAllPosts();

    @Query(value = "SELECT sum(view_count) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int getSumViewCountOfAllPosts();

    @Query(value = "SELECT time FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() order by time limit 1", nativeQuery = true)
    Date getFirstPublicationDateOfAllPosts();

    @Query(value = "SELECT count(*) FROM posts JOIN post_votes "
        + "ON post_votes.post_id = posts.id and post_votes.value = :value WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND posts.time<=NOW()", nativeQuery = true)
    int getVotesCountOfAllUser(@Param("value") int value);

    @Query(value = "SELECT count(*) FROM posts WHERE  posts.is_active = 1 "
        + "AND posts.moderation_status = 'NEW' AND time<=NOW()", nativeQuery = true)
    int getCountOfPostsForModeration();

}
