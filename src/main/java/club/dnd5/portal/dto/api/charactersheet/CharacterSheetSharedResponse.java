package club.dnd5.portal.dto.api.charactersheet;

import java.util.Map;

/**
 * Лист, открытый по ссылке: только чтение.
 * @param id идентификатор листа
 * @param name имя персонажа
 * @param data документ листа
 * @param own лист принадлежит тому, кто его открыл
 * @param savedId запись в «Других листах» открывшего, если он уже сохранил лист; иначе null
 */
public record CharacterSheetSharedResponse(String id, String name, Map<String, Object> data,
										   boolean own, String savedId) {
}
