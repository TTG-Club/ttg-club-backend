package club.dnd5.portal.dto.api;

import club.dnd5.portal.dto.api.classes.NameApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Builder
@Getter
@Setter
public class ValueApi extends NameApi {
	@Schema(description = "имя по русски")
	@NonNull
	private String rus;
	@NonNull
	@Schema(description = "имя по английски")
	private String eng;
	@Schema(description = "значение")
	private Object value;
}
