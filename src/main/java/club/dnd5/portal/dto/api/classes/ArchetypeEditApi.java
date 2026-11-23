package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.FeatureScaling;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
import lombok.Getter;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ArchetypeEditApi {
	private final Integer id;
	private final String name;
	private final String englishName;
	private final String genitiveName;
	/** Блоки разметки core-api. */
	private final List<String> description;
	private final byte level;
	private final SpellcasterType spellcasterType;
	private final String optionType;
	private final ArchetypeSpellLevelType spellLevelType;
	private final Short page;
	private final List<TraitApi> traits;
	private final List<FeatureLevelSaveApi> tableColumns;
	/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final ClassMechanics mechanics;
	/** Активные эффекты (Active Effects VTTG); {@code null} — нет. */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final List<ActiveEffect> activeEffects;

	public ArchetypeEditApi(Archetype archetype, List<ArchetypeTrait> archetypeTraits) {
		id = archetype.getId(); name = archetype.getName(); englishName = archetype.getEnglishName();
		genitiveName = archetype.getGenitiveName(); description = archetype.getDescription(); level = archetype.getLevel();
		spellcasterType = archetype.getSpellcasterType(); optionType = archetype.getOptionType() == null ? null : archetype.getOptionType().getCode();
		spellLevelType = archetype.getSpellLevelType() == null
			? ArchetypeSpellLevelType.CLASS_LEVEL
			: archetype.getSpellLevelType();
		page = archetype.getPage();
		mechanics = archetype.getMechanics();
		activeEffects = archetype.getActiveEffects();
		traits = archetypeTraits.stream().map(TraitApi::new).collect(Collectors.toList());
		tableColumns = archetype.getFeatureLevelDefenitions() == null
			? java.util.Collections.emptyList()
			: archetype.getFeatureLevelDefenitions().stream().map(FeatureLevelSaveApi::new).collect(Collectors.toList());
	}

	@Getter
	public static class TraitApi {
		private final Integer id; private final String name; private final String suffix; private final byte level;
		private final List<String> description; private final boolean optional = false; private final String child;
		/** Рост по уровням. */
		private final List<FeatureScaling> scaling;
		/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		private final ClassMechanics mechanics;
		/** Активные эффекты (Active Effects VTTG); {@code null} — нет. */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		private final List<ActiveEffect> activeEffects;
		private TraitApi(ArchetypeTrait trait) {
			id = trait.getId(); name = trait.getName(); suffix = trait.getSuffix(); level = trait.getLevel();
			description = trait.getDescription(); child = trait.getChild();
			scaling = trait.getScaling() == null ? java.util.Collections.emptyList() : trait.getScaling();
			mechanics = trait.getMechanics();
			activeEffects = trait.getActiveEffects();
		}
	}
}
