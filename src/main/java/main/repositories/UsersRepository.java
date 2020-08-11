package main.repositories;

import main.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository for working with users
 */

public interface UsersRepository extends CrudRepository<User, Integer> {

    /**
     * Checking email and password of user
     *
     * @param mail     email for checking
     * @param password for checkin
     * @return user object, if email and password are correct, otherwise null
     */

    @Query(value = "SELECT * FROM users WHERE users.email = :mail AND users.password = :password", nativeQuery = true)
    User checkMailAndPassword(@Param("mail") String mail, @Param("password") String password);

    /**
     * Checking email for free
     *
     * @param mail for checking
     * @return 1 - if email not free and 0 if email not founded
     */

    @Query(value = "SELECT count(*) FROM users WHERE users.email = :mail", nativeQuery = true)
    int checkFreeMail(@Param("mail") String mail);

    /**
     * Transform email of user to object User
     *
     * @param mail for searching
     * @return user object if email was founded, otherwise - null
     */


    @Query(value = "SELECT * FROM users WHERE users.email = :mail", nativeQuery = true)
    User getUserByEmail(@Param("mail") String mail);

    @Query(value = "SELECT * FROM users WHERE users.code = :code", nativeQuery = true)
    User getUserByCode(@Param("code") String code);

}
