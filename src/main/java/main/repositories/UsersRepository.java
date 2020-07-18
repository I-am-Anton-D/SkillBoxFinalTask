package main.repositories;

import main.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends CrudRepository<User, Integer> {
    @Query(value = "SELECT * FROM users WHERE users.email = :mail AND users.password = :password", nativeQuery = true)
    User checkMailAndPassword(@Param("mail") String mail, @Param("password") String password);

    @Query(value = "SELECT count(*) FROM users WHERE users.email = :mail", nativeQuery = true)
    int checkFreeMail(@Param("mail") String mail);

    @Query(value = "SELECT * FROM users WHERE users.email = :mail", nativeQuery = true)
    User getUserByEmail(@Param("mail") String mail);

}
