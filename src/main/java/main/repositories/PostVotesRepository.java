package main.repositories;

import main.model.PostVote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for working with votes of posts
 */

public interface PostVotesRepository extends CrudRepository<PostVote, Integer> {

    /**
     * Getting count of votes for posts
     *
     * @param postId id of post
     * @param value  1 = likes, -1 = dislike
     * @return count of votes
     */

    @Query(value = "SELECT count(*) FROM post_votes WHERE post_id = :postId and value=:value", nativeQuery = true)
    int getPostLikes(@Param("postId") int postId, @Param("value") int value);

    /**
     * Checking for vote of current user for specific post
     *
     * @param postId id of post
     * @param userId id of user
     * @param value  1 = likes, -1 = dislike
     * @return true if has voted, otherwise - false
     */

    @Query(value = "SELECT count(*) FROM post_votes WHERE user_id = :userId AND post_id = :postId AND value = :value", nativeQuery = true)
    int checkHasLike(@Param("postId") int postId, @Param("userId") int userId, @Param("value") int value);
}
