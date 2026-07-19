package club.dnd5.portal.dto.api.tracker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrackerRequest {

	@Size(max = 100)
	@Schema(description = "Название трекера (по умолчанию — «Новый трекер»)")
	private String name;

	@Schema(description = "Опция «новая инициатива каждый раунд»: true — перебрасывать инициативу всем "
			+ "живым в начале каждого раунда. При обновлении null — не менять; при создании по умолчанию false")
	private Boolean rerollEachRound;
}
