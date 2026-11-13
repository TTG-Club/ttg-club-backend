package club.dnd5.portal.dto.online;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Отметка о времени работы с инструментом сайта (например, «Ширмой Мастера»).
 */
@Getter
@Setter
public class OnlineUsageRequest {
	@NotBlank
	@Size(max = 32)
	private String feature;

	@NotBlank
	@Size(max = 128)
	private String key;

	@NotBlank
	@Pattern(regexp = "GUEST|REGISTERED")
	private String type;

	@NotNull
	@Min(0)
	@Max(300)
	private Integer activeSeconds;
}
