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
public class DmScreenListResponse {

	/** Ширмы пользователя, последние изменённые первее. */
	private List<DmScreenShortResponse> items;

	/** Сколько ширм у пользователя. */
	private int used;

	/** Сколько ширм можно иметь: 1 без подписки, 30 с подпиской или у администратора. */
	private int limit;
}
