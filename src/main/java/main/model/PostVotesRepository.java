package main.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PostVotesRepository extends CrudRepository<PostVote, Integer> {
    @Query(value = "SELECT count(*) FROM post_votes WHERE post_id = :postId and value=:value", nativeQuery = true)
    int getPostLikes(@Param("postId") int postId, @Param("value") int value);

    @Query(value = "SELECT count(*) FROM post_votes WHERE user_id = :userId AND post_id = :postId AND value = :value", nativeQuery = true)
    int checkHasLike(@Param("postId") int postId, @Param("userId") int userId, @Param("value") int value);
}
