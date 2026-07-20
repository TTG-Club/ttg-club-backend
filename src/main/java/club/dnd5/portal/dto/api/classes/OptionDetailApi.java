package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.model.classes.Option;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class OptionDetailApi extends OptionApi {
	private Integer id;
	private String requirements ;
	private String description;
	private List<ReferenceClassApi> classes;
	private String altName;
	private Integer level;
	private String prerequisite;
	private List<Option.OptionType> optionTypes;

	public OptionDetailApi(Option option) {
		super(option);
		url = null;
		id = option.getId();
		description = option.getDescription();
		altName = option.getAltName();
		level = option.getLevel();
		prerequisite = option.getPrerequisite();
		optionTypes = option.getOptionTypes();

		if (option.getLevel() != null)
		{
			requirements = String.format("%d уровень", option.getLevel());
			if (!"Нет".equals(option.getPrerequisite())){
				requirements += " и " + option.getPrerequisite();
			} 
		} else {
			requirements = option.getPrerequisite();
		}
		classes = option.getOptionTypes().stream().map(ReferenceClassApi::new).collect(Collectors.toList());
	}
}