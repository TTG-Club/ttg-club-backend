package club.dnd5.portal.dto.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiErrorResponse {
	private final int status;
	private final String error;
	private final String message;
	private final String path;
}
