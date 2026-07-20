package club.dnd5.portal.dto.api.spell;

import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.splells.MagicSchool;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

	@NotBlank
	private String description;

	private String upper;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;
}
