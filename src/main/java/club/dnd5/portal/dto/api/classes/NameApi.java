package club.dnd5.portal.dto.api.classes;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@RequiredArgsConstructor
public class NameApi {
	@NonNull
	private String rus;
	@NonNull
	private String eng;
	private String alt;
}