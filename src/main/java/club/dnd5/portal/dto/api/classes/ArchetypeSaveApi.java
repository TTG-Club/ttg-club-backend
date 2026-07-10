package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.Option;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArchetypeSaveApi {
	@NotBlank private String name;
	@NotBlank private String englishName;
	private String genitiveName;
	@NotBlank private String description;
	@Min(1) @Max(20) private byte level;
	private SpellcasterType spellcasterType;
	private Option.OptionType optionType;
	private Short page;
	private List<ClassTraitSaveApi> traits;
}
