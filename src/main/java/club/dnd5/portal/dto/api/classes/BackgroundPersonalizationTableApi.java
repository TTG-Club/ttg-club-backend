package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class BackgroundPersonalizationTableApi {
	private String name;
	private String formula;
	private String[] thead = new String[1];
	private Collection<Collection<String>> tbody;

	public BackgroundPersonalizationTableApi(PersonalizationType type, List<Personalization> values) {
		name = type.getName();
		formula = String.format("к%d", values.size());
		thead[0] = name;
		tbody = new ArrayList<>(values.size());
		for (int index = 0; index < values.size(); index++) {
			Collection<String> row = new ArrayList<>(2);
			row.add(String.valueOf(index + 1));
			row.add(values.get(index).getText());
			tbody.add(row);
		}
	}
}
