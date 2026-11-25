package club.dnd5.portal.dto.api.charactersheet;

/**
 * Доступ к листу по ссылке.
 * @param shareToken секрет ссылки {@code /tools/character-sheet/shared/<token>}
 */
public record CharacterSheetShareResponse(String shareToken) {
}
