package club.dnd5.portal.model.mechanics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Время накладывания для фильтра заклинаний механики ({@link SpellFilter#getCastingTime()}).
 *
 * <p>Порт словаря core-api ({@code CastingUnit}) с теми же значениями, а не старый
 * {@link club.dnd5.portal.model.TimeUnit}: в старом нет {@link #RITUAL} — на сайте 5e 2014
 * ритуал хранится флагом заклинания ({@code Spell.ritual}), а «Ритуальному заклинателю» нужен
 * именно он. Остальные общие значения ({@code ACTION}, {@code BONUS}, {@code REACTION},
 * {@code MINUTE}, {@code HOUR}, {@code ROUND}) совпадают с {@code TimeUnit} по имени.</p>
 */
@Getter
@RequiredArgsConstructor
public enum CastingUnit {
    BONUS("бонусное действие"),
    REACTION("реакция"),
    ACTION("действие"),
    ROUND("раунд"),
    MINUTE("минута"),
    RITUAL("ритуал"),
    HOUR("час"),
    SPECIAL("особое");

    private final String name;
}
