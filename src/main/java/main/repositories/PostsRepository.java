package main.repositories;

import java.util.Date;
import java.util.List;
import main.model.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for working with posts
 */

@Repository
public interface PostsRepository extends CrudRepository<Post, Integer> {

    /**
     * Getting list of visible post with offset and limit
     *
     * @param offset in list
     * @param limit  in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY posts.time DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getVisiblePosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting list of visible post in desc order by time with offset and limit
     *
     * @param offset in list
     * @param limit  in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY posts.time LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getVisiblePostsOrderDesc(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting list of popular post (maximum comments) in desc order by time with offset and limit
     *
     * @param offset in list
     * @param limit  in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY (SELECT count(*) FROM post_comments WHERE post_id=posts.id) DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPopularPosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting list of best post (maximum likes) in desc order by time with offset and limit
     *
     * @param offset in list
     * @param limit  in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() ORDER BY (SELECT count(*) FROM post_votes WHERE post_id = posts.id and value=1) DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getBestPosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting count of visible post
     *
     * @return count of visible post
     */


    @Query(value = "SELECT COUNT(*) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int countOfVisiblePosts();

    /**
     * Getting all visible posts
     *
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() ORDER BY posts.time", nativeQuery = true)
    List<Post> getAllVisiblePosts();

    /**
     * Getting list by tag ith offset and limit
     *
     * @param tagName name of tag
     * @param offset  in list
     * @param limit   in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts JOIN tag2post ON tag2post.post_id = posts.id "
        + "WHERE tag_id = (SELECT id FROM tags WHERE tags.name = :tagName) AND posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() "
        + "ORDER BY posts.time LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsByTag(@Param("tagName") String tagName, @Param("offset") int offset,
        @Param("limit") int limit);

    /**
     * Getting count of posts with current tag
     *
     * @param tagName tag name
     * @return count of posts with current tag
     */

    @Query(value = "SELECT COUNT(*) FROM posts JOIN tag2post ON tag2post.post_id = posts.id "
        + "WHERE tag_id = (SELECT id FROM tags WHERE tags.name = :tagName) AND posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int getPostsByTagCount(@Param("tagName") String tagName);

    /**
     * Searching in posts
     *
     * @param query  for searching
     * @param offset in list
     * @param limit  in list
     * @return list of post, where query was found
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND posts.text like :query OR  posts.title like :query ORDER BY posts.time DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsByQuery(@Param("query") String query, @Param("offset") int offset,
        @Param("limit") int limit);

    /**
     * Getting count of searched post by query
     *
     * @param query for searching
     * @return count of posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND posts.text like :query OR posts.title like :query", nativeQuery = true)
    int getCountOfPostsByQuery(@Param("query") String query);

    /**
     * Getting list of post by date with offset and limit
     *
     * @param date   in (yyyy-MM-dd) format
     * @param offset in list
     * @param limit  in list
     * @return list of posts with specific date creation
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND DATE(posts.time) = :date ORDER BY posts.time DESC "
        + "LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsByDate(@Param("date") String date, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting count of posts with specific date of creation
     *
     * @param date in (yyyy-MM-dd) format
     * @return count of posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() AND DATE(posts.time) = :date", nativeQuery = true)
    int getCountOfPostsByDate(@Param("date") String date);

    /**
     * Getting list of post for moderation with offset and limit
     *
     * @param status @see ModerationStatus enum
     * @param userId id of moderator
     * @param offset in list
     * @param limit  in list
     * @return list of post for moderation
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = :status "
        + "AND time<=NOW() AND (posts.moderator_id = 0 OR posts.moderator_id = :userId) ORDER BY posts.time "
        + "DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsForModeration(@Param("status") String status, @Param("userId") int userId,
        @Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting count of posts for moderation
     *
     * @param status @see ModerationStatus enum
     * @param userId id of moderator
     * @return count of posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = :status "
        + "AND time<=NOW() AND (posts.moderator_id = 0 OR posts.moderator_id = :userId)", nativeQuery = true)
    int getCountOfPostsForModeration(@Param("status") String status, @Param("userId") int userId);

    /**
     * Getting posts of user with offset and limit
     *
     * @param status @see ModerationStatus enum
     * @param userId id of user
     * @param active visibility of post
     * @param offset in list
     * @param limit  in list
     * @return list of posts
     */

    @Query(value = "SELECT * FROM posts WHERE posts.is_active = :active "
        + "AND posts.moderation_status = :status AND time<=NOW() AND posts.user_id = :userId ORDER BY posts.time "
        + "DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Post> getPostsOfUser(@Param("status") String status, @Param("userId") int userId,
        @Param("active") byte active,
        @Param("offset") int offset, @Param("limit") int limit);

    /**
     * Getting count of users posts
     *
     * @param status @see ModerationStatus enum
     * @param userId id of user
     * @param active visibility of post
     * @return count of post
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = :active "
        + "AND posts.moderation_status = :status AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getCountOfPostsOfUser(@Param("status") String status, @Param("userId") int userId,
        @Param("active") byte active);

    /**
     * Getting years of posts
     *
     * @return list of years
     */


    @Query(value = "SELECT DISTINCT year(time) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW();", nativeQuery = true)
    List<Integer> getYearsOfPost();


    /**
     * Getting distinct dates of posts
     *
     * @return list of dates
     */

    @Query(value = "SELECT DISTINCT date(time) FROM posts WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND time<=NOW() ORDER BY time;", nativeQuery = true)
    List<Date> getPostDates();

    /**
     * Getting count of posts with specific date
     *
     * @return count of posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = 'ACCEPTED' "
        + "AND time<=NOW() GROUP BY date(time) ORDER BY time", nativeQuery = true)
    List<Integer> getCountOfPostByDate();

    /**
     * Getting count of user posts
     *
     * @param userId id of user
     * @return count of user posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getCountOfUserPosts(@Param("userId") int userId);

    /**
     * Getting sum of views for posts of user
     *
     * @param userId id of user
     * @return sum of views
     */

    @Query(value = "SELECT sum(view_count) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getSumViewCountOfUserPosts(@Param("userId") int userId);

    /**
     * Getting date of first publication for user
     *
     * @param userId id of user
     * @return date of first post
     */

    @Query(value = "SELECT time FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() AND posts.user_id = :userId order by time limit 1", nativeQuery = true)
    Date getFirstPublicationDateOfUserPosts(@Param("userId") int userId);


    /**
     * Count of votes for users posts
     *
     * @param userId id of user
     * @param value  1 = like, -1 = dislike
     * @return count of likes
     */

    @Query(value = "SELECT count(*) FROM posts JOIN post_votes "
        + "ON post_votes.post_id = posts.id and post_votes.value = :value WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND posts.time<=NOW() AND posts.user_id = :userId", nativeQuery = true)
    int getVotesCountOfUser(@Param("userId") int userId, @Param("value") int value);

    /**
     * Getting count of all posts in blog
     *
     * @return count of all posts
     */

    @Query(value = "SELECT count(*) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int getCountOfAllPosts();

    /**
     * Getting sum views of all posts
     *
     * @return sum of views
     */

    @Query(value = "SELECT sum(view_count) FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW()", nativeQuery = true)
    int getSumViewCountOfAllPosts();

    /**
     * Getting date of first post in blog
     *
     * @return date of first post
     */

    @Query(value = "SELECT time FROM posts WHERE posts.is_active = 1 AND posts.moderation_status = "
        + "'ACCEPTED' AND time<=NOW() order by time limit 1", nativeQuery = true)
    Date getFirstPublicationDateOfAllPosts();

    /**
     * Getting count of all votes
     *
     * @param value 1 = like, -1 = dislike
     * @return count of all votes
     */

    @Query(value = "SELECT count(*) FROM posts JOIN post_votes "
        + "ON post_votes.post_id = posts.id and post_votes.value = :value WHERE posts.is_active = 1 "
        + "AND posts.moderation_status = 'ACCEPTED' AND posts.time<=NOW()", nativeQuery = true)
    int getVotesCountOfAllUser(@Param("value") int value);

    /**
     * Getting count of posts for moderation
     *
     * @return count of posts for moderation
     */

    @Query(value = "SELECT count(*) FROM posts WHERE  posts.is_active = 1 "
        + "AND posts.moderation_status = 'NEW' AND time<=NOW()", nativeQuery = true)
    int getCountOfPostsForModeration();

}
