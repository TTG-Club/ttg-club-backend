package club.dnd5.portal.dto.api.youtube;

import club.dnd5.portal.dto.api.RequestApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
public class YoutubeRequestApi extends RequestApi {
	@Schema(description = "filter", defaultValue = "null")
	public YoutubeRequestFilterApi filter;
}
