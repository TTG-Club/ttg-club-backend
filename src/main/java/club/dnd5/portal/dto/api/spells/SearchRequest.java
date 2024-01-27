package club.dnd5.portal.dto.api.spells;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
	@Schema(description = "строка поиска", example = "search")
	private String value;
	@Schema(description = "true если нужно точное совпадение", example = "false")
	private Boolean exact;
}
