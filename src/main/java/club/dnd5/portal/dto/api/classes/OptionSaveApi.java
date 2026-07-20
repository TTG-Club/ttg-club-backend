package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.classes.Option.OptionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class OptionSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	@NotEmpty
	private List<OptionType> optionTypes;
	private String prerequisite;
	private Integer level;
	@NotBlank
	private String description;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;
}
