package club.dnd5.portal.model.creature;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.DamageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(name= "action_damage")
public class Damage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String formula;
	private String context;

	@Enumerated(EnumType.STRING)
	private DamageType type;
}