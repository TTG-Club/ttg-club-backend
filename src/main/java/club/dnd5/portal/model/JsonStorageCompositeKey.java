package club.dnd5.portal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
public class JsonStorageCompositeKey implements Serializable {

	private Integer refId;

	private JsonType type;
}
