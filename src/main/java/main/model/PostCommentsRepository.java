package main.model;

import org.springframework.data.repository.CrudRepository;

public interface PostCommentsRepository extends CrudRepository<PostComment, Integer> {}
