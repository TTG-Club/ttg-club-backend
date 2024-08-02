package club.dnd5.portal.model.items;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "armors")
@Getter
public class Armor extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	private int AC;
	private int cost;
	private float weight;

	private Integer forceRequirements;
	private boolean stelsHindrance;

	@Enumerated(EnumType.ORDINAL)
	private ArmorCategory type;
	private String description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
}
