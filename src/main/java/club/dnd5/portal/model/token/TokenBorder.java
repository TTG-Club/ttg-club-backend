package club.dnd5.portal.model.token;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(
	name = "token_borders",
	indexes = {
		@Index(name = "token_border_name_index", columnList = "name"),
		@Index(name = "token_border_type_url_index", columnList = "type, url", unique = true)
	}
)
public class TokenBorder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false, unique = true)
	private String url;
}
