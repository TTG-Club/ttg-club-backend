package club.dnd5.portal.model.items;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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

	/** Описание. Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0027/0028); NULL — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName(){
		return StringUtil.getUrl(englishName);
	}
}
