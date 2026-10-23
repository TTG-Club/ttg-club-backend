package club.dnd5.portal.model.exporter;

import club.dnd5.portal.model.FoundryVersion;
import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "json_storage")
@Getter
@Setter
@IdClass(JsonStorageCompositeKey.class)
public class JsonStorage implements Serializable {
	@Id
	private Integer refId;

	// Не входит в ключ JsonStorageCompositeKey: Hibernate 7 требует, чтобы @Id совпадали с полями @IdClass.
	@Column(nullable = false)
	private String name;

	@Id
	@Enumerated(value = EnumType.STRING)
	private JsonType typeJson;

	@Id
	@Enumerated(EnumType.STRING)
	private FoundryVersion versionFoundry;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String jsonData;
}

