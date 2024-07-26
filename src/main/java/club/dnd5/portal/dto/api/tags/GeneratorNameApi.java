package club.dnd5.portal.dto.api.tags;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneratorNameApi {
	private CreatureTags creatureTags;
	private GenderTags genderTags;
	private int count;
}
