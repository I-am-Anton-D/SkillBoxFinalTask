package main.repositories;

import java.util.List;
import main.model.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for working with tags
 */

public interface TagsRepository extends CrudRepository<Tag, Integer> {

    /**
     * Transform name to id of tag
     *
     * @param tagName name of tag
     * @return id of tag
     */

    @Query(value = "SELECT id FROM tags WHERE tags.name = :tagName", nativeQuery = true)
    int getTagIdByName(@Param("tagName") String tagName);

    /**
     * Getting posts tags
     *
     * @param postId id of post
     * @return tags of post
     */

    @Query(value = "SELECT name FROM tags t JOIN (SELECT tag_id AS id FROM tag2post "
        + "WHERE post_id = :postId) t2 ON t.id = t2.id", nativeQuery = true)
    List<String> getPostTags(@Param("postId") int postId);
}
