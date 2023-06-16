package club.dnd5.portal.dto.api.youtube;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeRequestFilterApi {
	@Schema(description = "active or not", defaultValue = "null")
	private Boolean active;
}
