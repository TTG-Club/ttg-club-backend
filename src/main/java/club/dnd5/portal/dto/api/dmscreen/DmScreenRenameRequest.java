package club.dnd5.portal.dto.api.dmscreen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenRenameRequest {

	@NotBlank
	@Schema(description = "Новое название ширмы (после trim 1–64 символа)")
	private String name;
}
