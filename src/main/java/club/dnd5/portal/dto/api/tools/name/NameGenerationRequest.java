package club.dnd5.portal.dto.api.tools.name;

import club.dnd5.portal.model.races.Sex;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
public class NameGenerationRequest {
	@NotNull
	private NameGenerationType type;

	private NameGenerationFormat format;

	private Set<NameGenerationComponent> components;

	@Min(1)
	@Max(100)
	private int count = 1;

	private Integer raceId;
	private Set<Integer> raceIds;

	@NotNull
	@Size(min = 1)
	private Set<Sex> sexes;
}
