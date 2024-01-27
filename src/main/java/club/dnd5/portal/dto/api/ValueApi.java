package club.dnd5.portal.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Schema(description = "Имя и значение")
@Builder
@Getter
@Setter
public class ValueApi {
	@Schema(description = "имя по русски")
	@NonNull
	private String rus;
	@NonNull
	@Schema(description = "имя по английски")
	private String eng;
	@Schema(description = "значение")
	private Object value;
}
