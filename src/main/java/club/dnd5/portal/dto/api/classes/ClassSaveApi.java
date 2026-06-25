package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.Option;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ClassSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String accusativeName;
	@NotBlank
	private String description;
	@Min(1)
	@Max(20)
	private byte diceHp;
	private String armor;
	private String weapon;
	private String tools;
	private String savingThrows;
	private String archetypeName;
	private String equipment;
	private AbilityType spellAbility;
	private SpellcasterType spellcasterType;
	private List<AbilityType> primaryAbilities;
	private short enabledArhitypeLevel;
	@Min(0)
	private short skillAvailableCount;
	private List<SkillType> availableSkills;
	private Option.OptionType optionType;
	private Rest slotsReset;
	private boolean sidekick;
	private String icon;
	private Short page;
}
