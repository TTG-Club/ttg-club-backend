package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
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
	private List<ClassTraitSaveApi> classTraits;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public ClassSaveApi(HeroClass heroClass) {
		this(heroClass, heroClass.getTraits());
	}

	public ClassSaveApi(HeroClass heroClass, List<HeroClassTrait> traits) {
		name = heroClass.getName();
		englishName = heroClass.getEnglishName();
		accusativeName = heroClass.getAccusativeName();
		description = heroClass.getDescription();
		diceHp = heroClass.getDiceHp();
		armor = heroClass.getArmor();
		weapon = heroClass.getWeapon();
		tools = heroClass.getTools();
		savingThrows = heroClass.getSavingThrows();
		archetypeName = heroClass.getArchetypeName();
		equipment = heroClass.getEquipment();
		spellAbility = heroClass.getSpellAbility();
		spellcasterType = heroClass.getSpellcasterType();
		primaryAbilities = heroClass.getPrimaryAbilities();
		enabledArhitypeLevel = (short) heroClass.getEnabledArhitypeLevel();
		skillAvailableCount = heroClass.getSkillAvailableCount();
		availableSkills = heroClass.getAvailableSkills();
		optionType = heroClass.getOptionType();
		slotsReset = heroClass.getSlotsReset();
		sidekick = heroClass.isSidekick();
		icon = heroClass.getIcon();
		page = heroClass.getPage();
		classTraits = traits.stream().map(ClassTraitSaveApi::new).collect(java.util.stream.Collectors.toList());
		source = heroClass.getBook() == null ? null : heroClass.getBook().getSource();
	}
}
