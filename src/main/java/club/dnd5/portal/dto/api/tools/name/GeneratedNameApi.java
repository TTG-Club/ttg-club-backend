package club.dnd5.portal.dto.api.tools.name;

import club.dnd5.portal.model.races.Sex;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedNameApi {
	private final String value;
	private final String race;
	private final Sex sex;
}
