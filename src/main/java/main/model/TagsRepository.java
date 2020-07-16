package main.model;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TagsRepository extends CrudRepository<Tag, Integer> {

    @Query(value = "SELECT id FROM tags WHERE tags.name = :tagName", nativeQuery = true)
    int getTagIdByName(@Param("tagName") String tagName);

    @Query(value = "SELECT name FROM tags t JOIN (SELECT tag_id AS id FROM tag2post "
        + "WHERE post_id = :postId) t2 ON t.id = t2.id", nativeQuery = true)
    List<String> getPostTags(@Param("postId") int postId);
}
