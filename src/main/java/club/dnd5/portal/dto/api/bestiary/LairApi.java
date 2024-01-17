package club.dnd5.portal.dto.api.bestiary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.creature.Lair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class LairApi {
	@Schema(description = "описание логова")
	private String description;
	@Schema(description = "действия логова")
	private String action;
	@Schema(description = "эффекты в логове")
	private String effect;

	public LairApi(Lair lair){
		description = lair.getDescription();
		action = lair.getAction();
		effect = lair.getEffect();
	}
}
