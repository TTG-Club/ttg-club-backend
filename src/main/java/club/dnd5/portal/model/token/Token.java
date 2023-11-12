package club.dnd5.portal.model.token;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tokens",
	indexes = {
		@Index(name = "token_multi_index", columnList = "name, altName, englishName"),
		@Index(columnList = "url", unique = true),
		@Index(columnList = "refId")
	}
)
public class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	/**
	 * Id существа из бестиария
	 */
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
