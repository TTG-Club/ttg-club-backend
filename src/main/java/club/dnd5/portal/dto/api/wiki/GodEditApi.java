package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.god.God;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GodEditApi extends GodSaveApi {
	private Integer id;

	public GodEditApi(God god) {
		super(god);
		id = god.getId();
	}
}
