package club.dnd5.portal.model.creature;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "creature_feats")
@Data
public class CreatureFeat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;

	@Column(columnDefinition = "TEXT")
	private String description;
	/**
	 * Если умение содержит markdown
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean markdown;

	private String img;
}
