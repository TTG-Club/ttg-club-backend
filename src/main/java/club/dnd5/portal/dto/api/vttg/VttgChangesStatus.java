package club.dnd5.portal.dto.api.vttg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/** Ответ {@code /api/v2/vttg/changes/status}: сколько записей отдаст {@code /changes}. */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgChangesStatus {
	private final String since;
	private final String until;
	private final boolean hasUpdates;
	private final long count;
	private final Map<String, Long> byType;
	private final int schemaVersion;
}
