package club.dnd5.portal.dto.api.dmscreen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenShortResponse {

	private String id;

	private String name;

	private int windowCount;

	/** ISO-8601, например {@code 2026-11-12T10:15:30.123Z}. */
	private String updatedAt;
}
