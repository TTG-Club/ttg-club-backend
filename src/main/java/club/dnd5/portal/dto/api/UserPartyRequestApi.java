package club.dnd5.portal.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class UserPartyRequestApi extends RequestApi{
	@Schema(description = "фильтр")
	private UserPartyFilter filter;
}
