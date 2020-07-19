package main.repositories;

import main.model.CaptchaCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for working with captcha
 */

@Repository
public interface CaptchaCodesRepository extends CrudRepository<CaptchaCode, Integer> {

}
