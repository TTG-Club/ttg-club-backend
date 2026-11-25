package club.dnd5.portal.model.mechanics;

/**
 * Сколько зарядов возвращает ресурсу один вид отдыха.
 */
public enum CounterRestMode {
    /** Отдых ресурс не восстанавливает. */
    NONE,
    /** Отдых возвращает все заряды. */
    ALL,
    /** Отдых возвращает заданное число зарядов ({@link CounterRestRule#getAmount()}). */
    AMOUNT
}
