package club.dnd5.portal.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Builder
@Getter
@Setter
public class NameValueApi {
	@Schema(description = "ключ свойства", required = true, example = "STRENGTH")
	private Object key;
	@Schema(description = "название свойства")
	private String name;
	@Schema(description = "английское название свойства")
	private String englishName;
	@Schema(description = "краткое название свойства")
	private String shortName;
	@Schema(description = "значение свойства", example = "2")
	private Object value;
	@Schema(description = "дополнительный бонус если есть")
	private Object additional;
	@Schema(description = "флаг markdown")
	private Boolean markdown;
	@Schema(description = "сколько существ используют это действие")
	private Long sharedUsageCount;
}
