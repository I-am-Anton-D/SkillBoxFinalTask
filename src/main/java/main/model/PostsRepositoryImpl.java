package main.model;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PostsRepositoryImpl {

    @Autowired
    EntityManager entityManager;

    public int getVisiblePosts() {
        String sql = "SELECT count(*) FROM posts";
        Query query = entityManager.createQuery(sql);
        return (int) query.getSingleResult();

    }
}

