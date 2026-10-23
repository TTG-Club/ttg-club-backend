package club.dnd5.portal.model.tavern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.races.Sex;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tavern_owner_secret")
@Getter
@Setter
public class OwnerSecret {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String description;

	// null — подходит для любого пола хозяина
	@Enumerated(EnumType.STRING)
	private Sex sex;
}
