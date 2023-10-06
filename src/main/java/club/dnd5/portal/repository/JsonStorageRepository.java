package club.dnd5.portal.repository;

import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.Madness;
import club.dnd5.portal.model.MadnessType;
import club.dnd5.portal.model.exporter.JsonStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JsonStorageRepository extends JpaRepository<JsonStorage, JsonStorageCompositeKey>{
}
