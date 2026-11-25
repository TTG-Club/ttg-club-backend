package club.dnd5.portal.dto.api.charactersheet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetResponse {

	private String id;

	private String name;

	private Map<String, Object> data;

	private long version;

	/** ISO-8601, например {@code 2026-11-22T10:15:30.123Z}. */
	private String createdAt;

	/** ISO-8601. */
	private String updatedAt;

	/** Секрет ссылки на лист; null — доступ по ссылке выключен. */
	private String shareToken;
}
