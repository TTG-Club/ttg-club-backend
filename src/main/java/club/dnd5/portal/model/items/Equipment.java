package club.dnd5.portal.model.items;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter

@Entity
@Table(name = "equipments")
public class Equipment extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	private Integer cost;

	@Enumerated(EnumType.ORDINAL)
	private Currency currency;

	private Float weight;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ElementCollection(targetClass = EquipmentType.class)
	@JoinTable(name = "equipments_types", joinColumns = @JoinColumn(name = "equipment_id"))
	@Column(name = "type", nullable = false)
	@Enumerated(javax.persistence.EnumType.STRING)
	private Set<EquipmentType> types;

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
}
