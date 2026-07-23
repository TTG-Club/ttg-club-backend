package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.AbilityBonus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class RaceAbilitySaveApi {
	private Integer id;
	@NotNull
	private AbilityType ability;
	private Byte bonus;

	public RaceAbilitySaveApi(AbilityBonus abilityBonus) {
		id = abilityBonus.getId();
		ability = abilityBonus.getAbility();
		bonus = abilityBonus.getBonus();
	}
}
