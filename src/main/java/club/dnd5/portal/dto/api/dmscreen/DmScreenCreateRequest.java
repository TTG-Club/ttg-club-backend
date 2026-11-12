package club.dnd5.portal.dto.api.dmscreen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DmScreenCreateRequest {

	@NotBlank
	@Schema(description = "Название ширмы (после trim 1–64 символа)")
	private String name;

	@Size(max = 100)
	@Schema(description = "Окна ширмы (до 100); по умолчанию пусто")
	private List<DmScreenWindow> windows;
}
