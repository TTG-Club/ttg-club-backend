package club.dnd5.portal.dto.api.dmscreen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenResponse {

	private String id;

	private String name;

	private List<DmScreenWindow> windows;

	/** ISO-8601, например {@code 2026-11-12T10:15:30.123Z}. */
	private String updatedAt;
}
