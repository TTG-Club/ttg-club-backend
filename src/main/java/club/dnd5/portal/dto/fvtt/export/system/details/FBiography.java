package club.dnd5.portal.dto.fvtt.export.system.details;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FBiography {
	private String value;
    @JsonProperty("public")
	private String publicText;
}
