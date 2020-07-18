package main.repositories;

import java.util.List;
import main.model.PostComment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PostCommentsRepository extends CrudRepository<PostComment, Integer> {

    @Query(value = "SELECT * FROM post_comments WHERE post_id = :postId", nativeQuery = true)
    List<PostComment> getPostComments(@Param("postId") int postId);
}
