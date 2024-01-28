package club.dnd5.portal.model.creature;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter

@Entity
@Table(name = "creature_actions")
public class Action {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.ORDINAL)
	private ActionType actionType;

	/**
	 * Если действие содержит markdown
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean markdown;
}
