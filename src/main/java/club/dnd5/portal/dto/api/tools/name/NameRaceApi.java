package club.dnd5.portal.dto.api.tools.name;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NameRaceApi {
	private final Integer id;
	private final String name;
	private final Integer parentId;
	private final boolean available;
}
