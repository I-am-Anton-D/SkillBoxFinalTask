package main.model;

import org.springframework.data.repository.CrudRepository;
public interface PostsRepository extends CrudRepository<Post,Integer> {}
