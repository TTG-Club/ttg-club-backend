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
	private String requirements ;
	private String description;
	private List<ReferenceClassApi> classes;

	public OptionDetailApi(Option option) {
		super(option);
		url = null;
		description = option.getDescription();

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