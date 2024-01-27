package club.dnd5.portal.model.classes.archetype;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import org.thymeleaf.util.StringUtils;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
@Table(name = "hero_class_feats")
public class ArchetypeTrait {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@Column(columnDefinition = "varchar(25) default ''")
	private String suffix;
	private byte level;
	@Column(columnDefinition = "TEXT")
	private String description;
	private String child;

	@ManyToOne
	private Archetype archetype;

	@OneToMany(fetch = FetchType.LAZY)
	private List<Spell> spells;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getCapitalizeName() {
		return StringUtils.capitalizeWords(name.toLowerCase());
	}
	public String getDisplayLevel() {
		String suffix = "ый";
		switch (level) {
		case 2:
		case 6:
		case 7:
		case 8:
			suffix = "ой";
			break;
		case 3:
			suffix = "ий";
			break;
		default:
			suffix = "ый";
			break;
		}
		return String.format("%d-%s уровень", level, suffix);
	}
}
