package club.dnd5.portal.repository;

import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.exporter.JsonStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsonStorageRepository extends JpaRepository<JsonStorage, JsonStorageCompositeKey>{
	JsonStorage findByName(String name);
}
