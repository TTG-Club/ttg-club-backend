package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArchetypeSaveApi {
	@NotBlank private String name;
	@NotBlank private String englishName;
	private String genitiveName;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty @JsonDeserialize(using = MarkupBlocksDeserializer.class) private List<@NotBlank String> description;
	@Min(1) @Max(20) private byte level;
	private SpellcasterType spellcasterType;
	private String optionType;
	private ArchetypeSpellLevelType spellLevelType = ArchetypeSpellLevelType.CLASS_LEVEL;
	private Short page;
	private List<@Valid ClassTraitSaveApi> traits;
	private List<@Valid FeatureLevelSaveApi> tableColumns;
	/** Механика для листа персонажа (модель core-api), сохраняется целиком; {@code null} — оставить как есть. */
	@Valid
	private ClassMechanics mechanics;
	/** Активные эффекты (Active Effects VTTG), сохраняются целиком; {@code null} — оставить как есть. */
	private List<@Valid ActiveEffect> activeEffects;

	public ArchetypeSaveApi(Archetype archetype) {
		this(archetype, archetype.getFeats());
	}

	public ArchetypeSaveApi(Archetype archetype, List<ArchetypeTrait> archetypeTraits) {
		name = archetype.getName();
		englishName = archetype.getEnglishName();
		genitiveName = archetype.getGenitiveName();
		description = archetype.getDescription();
		level = archetype.getLevel();
		spellcasterType = archetype.getSpellcasterType();
		optionType = archetype.getOptionType() == null ? null : archetype.getOptionType().getCode();
		spellLevelType = archetype.getSpellLevelType() == null
			? ArchetypeSpellLevelType.CLASS_LEVEL
			: archetype.getSpellLevelType();
		page = archetype.getPage();
		mechanics = archetype.getMechanics();
		activeEffects = archetype.getActiveEffects();
		traits = archetypeTraits.stream().map(ClassTraitSaveApi::new).collect(java.util.stream.Collectors.toList());
		tableColumns = archetype.getFeatureLevelDefenitions() == null
			? java.util.Collections.emptyList()
			: archetype.getFeatureLevelDefenitions().stream().map(FeatureLevelSaveApi::new).collect(java.util.stream.Collectors.toList());
	}
}
