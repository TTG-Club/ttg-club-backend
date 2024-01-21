package club.dnd5.portal.dto.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenBorderApi {
	private Long id;
	private String name;
	private String type;
	private String url;
}
