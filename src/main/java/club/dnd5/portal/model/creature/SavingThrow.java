package club.dnd5.portal.model.creature;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.AbilityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter

@Entity
@Table(name = "creature_saving_throws")
@NoArgsConstructor
public class SavingThrow {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Enumerated(EnumType.STRING)
	private AbilityType ability;
	private byte bonus;
	private String additionalBonus;

	public SavingThrow(AbilityType ability, byte bonus){
		this.ability = ability;
		this.bonus = bonus;
	}

	public String getText() {
		return String.format("%s %+d ", ability.getShortName(), bonus);
	}
}