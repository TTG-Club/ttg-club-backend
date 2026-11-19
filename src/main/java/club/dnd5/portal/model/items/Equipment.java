package club.dnd5.portal.model.items;

import java.util.HashSet;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Set;

@Getter
@Setter

@Entity
@Table(name = "equipments")
public class Equipment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;
	private String altName;
	private Integer cost;

	@Enumerated(EnumType.ORDINAL)
	private Currency currency;

	private Float weight;

	@Column(columnDefinition = "TEXT")
	private String description;

	// категории — в самой строке (jsonb), отдельных запросов на запись больше нет
	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0017
	@Column(columnDefinition = "jsonb", nullable = false)
	private Set<EquipmentType> types = new HashSet<>();

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getTextCost() {
		if (getCost() == null) {
			return null;
		}
		else
		{

			switch (getCurrency()) {
			case SM:
				return getCost() / 10f + " " + getCurrency().getName();
			case GM:
				return getCost() / 100f + " " + getCurrency().getName();
			case PM:
				return getCost() / 1000f + " " + getCurrency().getName();
			default:
				return getCost() + " " + getCurrency().getName();
			}
		}
	}

	public String getUrlName(){
		return StringUtil.getUrl(englishName);
	}

	/** jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullJsonListsToEmpty() {
		types = types == null ? new HashSet<>() : types;
	}
}
