package club.dnd5.portal.dto.api.charactersheet;

import java.util.List;

/**
 * Кто ещё держит лист открытым и какая версия листа сейчас на сервере — ответ на отметку присутствия.
 * По версии клиент узнаёт, что лист сохранил другой редактор, и подтягивает его правки.
 * @param users другие пользователи, у которых лист открыт прямо сейчас; сам спросивший не входит
 * @param version текущая версия листа
 */
public record CharacterSheetPresenceResponse(List<CharacterSheetPresenceUserResponse> users, long version) {
}
