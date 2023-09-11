package club.dnd5.portal.model.encounters;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.HabitatType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity()
@Table(name = "random_encounter_tables")
public class RandomEncounterTable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String name;

	private Integer level;
	private String formula;

	@Enumerated (EnumType.STRING)
	private HabitatType type;
	private String environment;

	@OneToMany
	@JoinColumn(name = "encounter_id")
	private List<RandomEncounterRow> encounters;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
}
