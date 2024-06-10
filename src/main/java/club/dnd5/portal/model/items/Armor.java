package club.dnd5.portal.model.items;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "armors")
@Getter
public class Armor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;
	private String altName;
	private int AC;
	private int cost;
	private float weight;

	@Column(nullable = true)
	private Integer forceRequirements;
	private boolean stelsHindrance;

	@Enumerated(EnumType.ORDINAL)
	private ArmorCategory type;
	private String description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName(){
		return StringUtil.getUrl(englishName);
	}
}
