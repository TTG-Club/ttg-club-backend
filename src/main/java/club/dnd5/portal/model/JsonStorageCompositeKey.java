package club.dnd5.portal.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class JsonStorageCompositeKey implements Serializable {

	private Integer refId;

	private JsonType typeJson;
}
