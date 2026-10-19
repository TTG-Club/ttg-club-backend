package club.dnd5.portal.dto.api.vttg;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Ответ {@code /api/v2/vttg/changes} — в той же форме, что у нового сайта.
 *
 * <p>Времени изменения у записей старого сайта нет, поэтому дельты тоже нет: полная
 * выгрузка уходит на запрос с начала времён, а на запрос с курсора — пустой список.
 * Об изменениях VTTG узнаёт по {@code schemaVersion} — это отпечаток содержимого
 * выгрузки: сменился он — VTTG перекачивает компендиум целиком.</p>
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgChangesResponse {
	private final String until;
	private final List<VttgChange> upserts;
	private final List<Map<String, Object>> sections;
	private final List<VttgSource> sources;
	private final int schemaVersion;
}
