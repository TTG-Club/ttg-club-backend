package club.dnd5.portal.repository;

import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsonStorageRepository extends JpaRepository<JsonStorage, JsonStorageCompositeKey>{
	JsonStorage findByName(String name);
	List<JsonStorage> findJsonStoragesByTypeJsonAndVersionFoundry(JsonType jsonType, Integer versionFoundry);
}
