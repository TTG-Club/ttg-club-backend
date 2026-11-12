package club.dnd5.portal.model.items;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "armors")
@Getter
@Setter
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

	/**
	 * В базе колонка TEXT. Без явного типа Hibernate считал её varchar(255) и на каждом
	 * запуске (ddl-auto=update) пытался сжать: ALTER на armors мог обрезать описания и
	 * вешал запуск, если таблицу держала чужая транзакция.
	 */
	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName(){
		return StringUtil.getUrl(englishName);
	}
}
