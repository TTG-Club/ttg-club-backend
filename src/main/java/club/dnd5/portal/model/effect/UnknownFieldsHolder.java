package club.dnd5.portal.model.effect;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-объект, который хранит ключи, незнакомые модели, и отдаёт их обратно как
 * есть.
 * <p>
 * Модель эффекта развивается на стороне системы D&amp;D быстрее бэкенда: без этого
 * поле, которого модель ещё не знает, Jackson молча выбрасывал бы при каждом
 * сохранении, и редактор сайта портил бы уже заполненную механику.
 * <p>
 * Незнакомые ключи пишутся после известных полей, в том порядке, в каком пришли.
 * Пустая карта ничего не добавляет в JSON. Методы названы не как бин-свойство
 * ({@code getX}/{@code setX}), чтобы ни Jackson, ни MapStruct, ни схема OpenAPI
 * не приняли карту за обычное поле.
 * <p>
 * Порт core-api: значения хранятся как {@code Object} (карты, списки, строки, числа), а не
 * {@code JsonNode}. Так класс не привязан к версии Jackson: веб-слой здесь на Jackson 3
 * ({@code tools.jackson}), а jsonb-колонки сериализует Hibernate своим маппером; аннотации
 * {@code com.fasterxml.jackson.annotation} понимают оба.
 */
public abstract class UnknownFieldsHolder {
    private final Map<String, Object> unknownFields = new LinkedHashMap<>();

    /** Незнакомые модели ключи в порядке прихода; только для чтения. */
    @JsonAnyGetter
    public Map<String, Object> unknownFields() {
        return Collections.unmodifiableMap(unknownFields);
    }

    /** Запоминает ключ, которого нет среди полей модели. */
    @JsonAnySetter
    public void putUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }
}
