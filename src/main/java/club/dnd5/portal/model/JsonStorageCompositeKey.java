package club.dnd5.portal.model;

import club.dnd5.portal.model.exporter.JsonStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JsonStorageCompositeKey implements Serializable {

	private Integer refId;

	private JsonType typeJson;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JsonStorageCompositeKey that = (JsonStorageCompositeKey) o;
		return refId.equals(that.refId) && typeJson == that.typeJson;
	}

	@Override
	public int hashCode() {
		return Objects.hash(refId, typeJson);
	}
}
