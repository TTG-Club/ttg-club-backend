package club.dnd5.portal.dto.fvtt.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class FAC {
	@NonNull private byte flat;
	private String calc = "default";
	private String formula = "";
	private Byte min;
}
