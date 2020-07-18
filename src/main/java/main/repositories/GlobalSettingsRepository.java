package main.repositories;

import main.model.GlobalSettings;
import org.springframework.data.repository.CrudRepository;
public interface GlobalSettingsRepository  extends CrudRepository<GlobalSettings, Integer> {}
