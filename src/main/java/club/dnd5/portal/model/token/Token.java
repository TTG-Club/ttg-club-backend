package club.dnd5.portal.model.token;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tokens",
	indexes = {
		@Index(name = "token_multi_index", columnList = "name, altName, englishName, type"),
		@Index(columnList = "url", unique = true)
	}
)
public class Token {
	/**
	 * Id существа из бестиария
	 */
	@Id
	@Column(nullable = false)
	private Integer refId;
	@Column(nullable = false)
	private String name;
	private String altName;
	@Column(nullable = false)
	private String englishName;
	/**
	 * Типы: круглый, гекс, сверху
	 */
	@Column(nullable = false)
	private String type;
	@Column(nullable = false)
	private String url;
}
