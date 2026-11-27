package club.dnd5.portal.dto.api.charactersheet;

import java.util.List;

/**
 * Запросы и редакторы одного листа — для окна «Поделиться» у владельца.
 * @param limit максимум редакторов у листа
 * @param editors неотвеченные запросы и выданные права, старые первее
 */
public record CharacterSheetEditorListResponse(int limit, List<CharacterSheetEditorResponse> editors) {
}
