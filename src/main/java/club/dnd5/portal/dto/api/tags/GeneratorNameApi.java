package club.dnd5.portal.dto.api.tags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GeneratorNameApi {
	@JsonProperty("type")
	private String creatureTag;
	@JsonProperty("tags")
	private List<String> creatureNameTags;
	@JsonProperty("gender")
	private List<String> genderTags;
	private int count;
}
