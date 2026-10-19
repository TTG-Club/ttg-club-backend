package club.dnd5.portal.dto.api.vttg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Источник в словаре VTTG: записи везут только {@code sourceKey}, подпись берётся отсюда. */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSource {
	private final String key;
	private final String name;
	private final String nameEn;
	private final String abbreviation;
}
