package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import jakarta.validation.Valid;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import club.dnd5.portal.model.classes.FeatureOption;
import club.dnd5.portal.model.classes.FeatureOptionsChoice;
import club.dnd5.portal.model.classes.FeatureScaling;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class ClassTraitSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String suffix;
	@Min(1)
	@Max(20)
	private byte level;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
	private boolean optional;
	private String child;
	/** Тип списка вариантов; пусто — у умения нет вариантов. */
	private String optionsType;
	/** Название списка вариантов. */
	private String optionsName;
	/** Варианты умения; {@code null} — оставить как есть. */
	@Valid
	private List<@Valid FeatureOption> options;
	/** Выбор из вариантов; {@code null} — варианты только справка. */
	@Valid
	private FeatureOptionsChoice optionsChoice;
	/** Рост по уровням; {@code null} — оставить как есть. */
	@Valid
	private List<@Valid FeatureScaling> scaling;
	/** Механика для листа персонажа (модель core-api), сохраняется целиком; {@code null} — оставить как есть. */
	@Valid
	private ClassMechanics mechanics;
	/** Активные эффекты (Active Effects VTTG), сохраняются целиком; {@code null} — оставить как есть. */
	@Valid
	private List<ActiveEffect> activeEffects;

	public ClassTraitSaveApi(HeroClassTrait trait) {
		id = trait.getId();
		name = trait.getName();
		suffix = trait.getSuffix();
		level = trait.getLevel();
		description = trait.getDescription();
		optional = trait.getOptional() != 0;
		child = trait.getChild();
		scaling = trait.getScaling();
		optionsType = trait.getOptionsType();
		optionsName = trait.getOptionsName();
		options = trait.getOptions();
		optionsChoice = trait.getOptionsChoice();
		mechanics = trait.getMechanics();
		activeEffects = trait.getActiveEffects();
	}

	public ClassTraitSaveApi(ArchetypeTrait trait) {
		id = trait.getId();
		name = trait.getName();
		suffix = trait.getSuffix();
		level = trait.getLevel();
		description = trait.getDescription();
		child = trait.getChild();
		scaling = trait.getScaling();
		optionsType = trait.getOptionsType();
		optionsName = trait.getOptionsName();
		options = trait.getOptions();
		optionsChoice = trait.getOptionsChoice();
		mechanics = trait.getMechanics();
		activeEffects = trait.getActiveEffects();
	}
}
