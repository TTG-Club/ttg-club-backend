package club.dnd5.portal.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchApi {
	@NonNull private Object name;
	@NonNull private Object englishName;
	@NonNull private Object section;
	@NonNull private Object url;
	private Object description;
	private SourceApi source;
}
