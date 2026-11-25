package club.dnd5.portal.dto.api.charactersheet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CharacterSheetListResponse {

	/** Листы пользователя с документами, последние изменённые первее. */
	private List<CharacterSheetResponse> items;

	/** Сколько листов у пользователя. */
	private int used;

	/** Сколько листов можно иметь. */
	private int limit;

	/** Сколько листов можно иметь с подпиской. */
	private int subscriberLimit;
}
