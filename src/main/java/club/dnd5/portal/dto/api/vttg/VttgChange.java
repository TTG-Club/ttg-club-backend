package club.dnd5.portal.dto.api.vttg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Одна запись компендиума VTTG.
 *
 * <p>{@code type} — раздел сайта, {@code url} — слаг записи, {@code data} — сама запись
 * в формате VTTG. Раскладывает запись VTTG по полям {@code data}.</p>
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgChange {
	private final String type;
	private final String url;
	private final Map<String, Object> data;
}
