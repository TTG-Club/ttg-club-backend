package club.dnd5.portal.dto.api.tools;

import club.dnd5.portal.model.encounters.RandomEncounterRow;
import club.dnd5.portal.model.encounters.RandomEncounterTable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class RandomEncounterTableApi {
	private String name;
	private String formula;
	private String[] thead = new String[1];
	private Collection<Collection<String>> tbody;

	public RandomEncounterTableApi(RandomEncounterTable table) {
		name = table.getName();
		formula = table.getFormula();
		thead[0] = "Столкновение";
		tbody = new ArrayList<>(table.getEncounters().size());
		for (RandomEncounterRow reRow : table.getEncounters()) {
			Collection<String> row = new ArrayList<>(2);
			row.add(reRow.getEnd() == reRow.getStart() ? String.valueOf(reRow.getStart()) : String.format("%d-%d", reRow.getEnd(), reRow.getStart()));
			row.add(reRow.getDescription());
			tbody.add(row);
		}
	}
}
