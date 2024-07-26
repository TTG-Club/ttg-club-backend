package club.dnd5.portal.dto.api.tags;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GenderTags {
	private String tag;
	private List<String> genders;
}
