package club.dnd5.portal.model.encounters;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.HabitatType;
import lombok.Getter;
import lombok.Setter;

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

	public String getK100() {
		if (start == end) {
			return String.format("%02d", start);
		}
		return String.format("%02d-%02d", end, start);
	}
}
