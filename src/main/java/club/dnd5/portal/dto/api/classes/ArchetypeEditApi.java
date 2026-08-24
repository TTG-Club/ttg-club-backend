package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SpellcasterType;
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
	private final String description;
	private final byte level;
	private final SpellcasterType spellcasterType;
	private final Option.OptionType optionType;
	private final ArchetypeSpellLevelType spellLevelType;
	private final Short page;
	private final List<TraitApi> traits;
	private final List<FeatureLevelSaveApi> tableColumns;

	public ArchetypeEditApi(Archetype archetype, List<ArchetypeTrait> archetypeTraits) {
		id = archetype.getId(); name = archetype.getName(); englishName = archetype.getEnglishName();
		genitiveName = archetype.getGenitiveName(); description = archetype.getDescription(); level = archetype.getLevel();
		spellcasterType = archetype.getSpellcasterType(); optionType = archetype.getOptionType();
		spellLevelType = archetype.getSpellLevelType() == null
			? ArchetypeSpellLevelType.CLASS_LEVEL
			: archetype.getSpellLevelType();
		page = archetype.getPage();
		traits = archetypeTraits.stream().map(TraitApi::new).collect(Collectors.toList());
		tableColumns = archetype.getFeatureLevelDefenitions() == null
			? java.util.Collections.emptyList()
			: archetype.getFeatureLevelDefenitions().stream().map(FeatureLevelSaveApi::new).collect(Collectors.toList());
	}

	@Getter
	public static class TraitApi {
		private final Integer id; private final String name; private final String suffix; private final byte level;
		private final String description; private final boolean optional = false; private final String child;
		private TraitApi(ArchetypeTrait trait) {
			id = trait.getId(); name = trait.getName(); suffix = trait.getSuffix(); level = trait.getLevel();
			description = trait.getDescription(); child = trait.getChild();
		}
	}
}
