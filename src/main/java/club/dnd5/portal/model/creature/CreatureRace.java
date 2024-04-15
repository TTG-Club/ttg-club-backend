package club.dnd5.portal.model.creature;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "creature_races")
@Getter
@Setter
public class CreatureRace {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@Column(columnDefinition = "TEXT")
	private String description;
}
