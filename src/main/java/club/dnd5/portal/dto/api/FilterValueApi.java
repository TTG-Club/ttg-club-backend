package club.dnd5.portal.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@EqualsAndHashCode
public class FilterValueApi {
    private String label; // отображаемое название
    private Object key; // ключ для сохранения и запроса
	@JsonProperty("default")
    private Boolean defaultValue; // значение по-умолчанию
    private String tooltip; // подсказка при наведении

    public FilterValueApi(String label, Object key, Boolean defaultValue) {
		this.label = label;
		this.key = key;
		this.defaultValue = defaultValue;
	}

    public FilterValueApi(String label, Object key) {
		this.label = label;
		this.key = key;
		this.defaultValue = Boolean.FALSE;
	}
}
