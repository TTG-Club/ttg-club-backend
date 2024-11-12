package club.dnd5.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HealType {
    /** Хиты. */
    HP("Хиты"),
    /* Временные хиты */
    THP("Временные хиты");
    private final String name;
}
