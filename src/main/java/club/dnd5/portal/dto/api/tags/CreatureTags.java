package club.dnd5.portal.dto.api.tags;

import club.dnd5.portal.model.CreatureType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreatureTags {
	private CreatureType type;
	private List<String> tags;
}
