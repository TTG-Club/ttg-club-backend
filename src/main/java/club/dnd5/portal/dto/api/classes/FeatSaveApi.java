package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.mechanics.FeatMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import jakarta.validation.Valid;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.trait.Trait;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class FeatSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private Integer level;
	private String requirement;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
	private List<AbilityType> abilities;
	private List<SkillType> skills;

	/** Механика для листа персонажа (модель core-api), сохраняется целиком; {@code null} — оставить как есть. */
	@Valid
	private FeatMechanics mechanics;
	/** Активные эффекты (Active Effects VTTG), сохраняются целиком; {@code null} — оставить как есть. */
	@Valid
	private List<ActiveEffect> activeEffects;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public FeatSaveApi(Trait trait) {
		name = trait.getName();
		englishName = trait.getEnglishName();
		altName = trait.getAltName();
		level = trait.getLevel();
		requirement = trait.getRequirement();
		description = trait.getDescription();
		abilities = trait.getAbilities();
		skills = trait.getSkills();
		mechanics = trait.getMechanics();
		activeEffects = trait.getActiveEffects();
		source = trait.getBook() == null ? null : trait.getBook().getSource();
	}
}
