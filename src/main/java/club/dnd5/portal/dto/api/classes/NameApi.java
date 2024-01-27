package club.dnd5.portal.dto.api.classes;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@RequiredArgsConstructor
public class NameApi {
	@Schema(description = "имя по русски", required = true)
	@NonNull
	private String rus;
	@NonNull
	@Schema(description = "имя по английски", required = true)
	private String eng;
	@Schema(description = "альтернативное имя")
	private String alt;
}