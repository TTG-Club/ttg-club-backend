package club.dnd5.portal.dto.api;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class TokenBorderApi {
	@NonNull
	private Long id;
	private String name;
	private String type;
	private String url;
}
