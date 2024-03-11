package club.dnd5.portal.repository;

import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JsonStorageRepository extends JpaRepository<JsonStorage, JsonStorageCompositeKey>{
	Optional<JsonStorage> findByName(String name);
	List<JsonStorage> findAllByTypeJsonAndVersionFoundry(JsonType jsonType, Integer versionFoundry);
}
