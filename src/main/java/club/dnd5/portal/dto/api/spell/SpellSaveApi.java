package club.dnd5.portal.dto.api.spell;

import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.SpellDamage;
import club.dnd5.portal.model.splells.TimeCast;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
public class SpellSaveApi {
	@NotBlank
	private String name;

	@NotBlank
	private String englishName;

	@Min(0)
	@Max(9)
	private byte level;

	@NotNull
	private MagicSchool school;

	private String additionalType;
	private Boolean ritual;
	private Boolean concentration;

	private boolean verbalComponent;
	private boolean somaticComponent;
	private Boolean consumable;
	private String materialComponent;

	@Min(1)
	private int timeNumber = 1;

	@NotNull
	private TimeUnit timeUnit = TimeUnit.ACTION;

	private String timeCondition;

	@NotBlank
	private String range;

	@NotBlank
	private String duration;

	/** Блоки разметки core-api. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;

	/** Блоки разметки «На больших уровнях»; пусто — нет. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> upper;

	/** Урон для листа персонажа; {@code null} или без бросков — урона нет. */
	@Valid
	private SpellDamage damage;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public SpellSaveApi(Spell spell) {
		name = spell.getName();
		englishName = spell.getEnglishName();
		level = spell.getLevel();
		school = spell.getSchool();
		additionalType = spell.getAdditionalType();
		ritual = spell.getRitual();
		concentration = spell.getConcentration();
		verbalComponent = spell.isVerbalComponent();
		somaticComponent = spell.isSomaticComponent();
		consumable = spell.getConsumable();
		materialComponent = spell.getAdditionalMaterialComponent();
		if (spell.getTimes() != null && !spell.getTimes().isEmpty()) {
			TimeCast time = spell.getTimes().get(0);
			timeNumber = time.getNumber();
			timeUnit = time.getUnit();
			timeCondition = time.getCondition();
		}
		range = spell.getDistance();
		duration = spell.getDuration();
		description = spell.getDescription();
		upper = spell.getUpperLevel();
		damage = spell.getDamage();
		source = spell.getBook() == null ? null : spell.getBook().getSource();
	}
}
