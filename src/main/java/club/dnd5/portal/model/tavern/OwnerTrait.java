package club.dnd5.portal.model.tavern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import club.dnd5.portal.model.races.Sex;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tavern_owner_traits")
@Getter
@Setter
public class OwnerTrait {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String description;

	// null — подходит для любого пола хозяина
	@Enumerated(EnumType.STRING)
	private Sex sex;
}
