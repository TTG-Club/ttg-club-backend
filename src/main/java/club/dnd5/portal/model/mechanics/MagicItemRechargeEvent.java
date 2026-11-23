package club.dnd5.portal.model.mechanics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Событие, по которому магический предмет восстанавливает заряды.
 */
@Getter
@AllArgsConstructor
public enum MagicItemRechargeEvent {
    DAWN("на рассвете"),
    SHORT_REST("после короткого отдыха"),
    LONG_REST("после продолжительного отдыха");

    private final String name;
}
