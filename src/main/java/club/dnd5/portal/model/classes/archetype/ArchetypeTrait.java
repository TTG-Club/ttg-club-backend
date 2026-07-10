package club.dnd5.portal.model.classes.archetype;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
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

}
