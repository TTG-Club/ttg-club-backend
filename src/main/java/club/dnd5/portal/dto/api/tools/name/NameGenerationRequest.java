package club.dnd5.portal.dto.api.tools.name;

import club.dnd5.portal.model.races.Sex;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
public class NameGenerationRequest {
	@NotNull
	private NameGenerationType type;

	@NotNull
	private NameGenerationFormat format;

	@Min(1)
	@Max(100)
	private int count = 1;

	private Integer raceId;

	@NotNull
	@Size(min = 1)
	private Set<Sex> sexes;
}
