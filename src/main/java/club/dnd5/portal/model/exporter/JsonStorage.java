package club.dnd5.portal.model.exporter;

import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "json_storage")
@Getter
@Setter
@IdClass(JsonStorageCompositeKey.class)
public class JsonStorage implements Serializable {

	@Id
	private Integer refId;

	@Id
	private String name;

	@Id
	@Enumerated(value = EnumType.STRING)
	private JsonType typeJson;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String jsonData;
}

