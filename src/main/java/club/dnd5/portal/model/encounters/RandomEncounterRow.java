package club.dnd5.portal.model.encounters;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.HabitatType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Setter

@Entity()
@Table(name = "random_encounters",
	indexes = {@Index(name = "fn_index", columnList = "start, end, level, type")}
)
public class RandomEncounterRow {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private int start;
	private int end;

	private int level;

	@Enumerated (EnumType.STRING)
	private HabitatType type;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private RandomEncounterTable encounter;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

}
