package main.repositories;

import main.model.GlobalSettings;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for working with global settings
 */

public interface GlobalSettingsRepository extends CrudRepository<GlobalSettings, Integer> {

}
