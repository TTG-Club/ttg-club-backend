package club.dnd5.portal.dto.api.spells;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellFilter {
	@JsonProperty("book")
	private List<String> books;
	
	@JsonProperty("level")
	private List<Integer> levels;

	@JsonProperty("class") 
    private List<String> myclass;

    @JsonProperty("school") 
    private List<String> schools;

    private List<String> timecast;
    private List<String> distance;
    private List<String> duration;

    private List<String> components;
    private List<String> ritual;
    private List<String> concentration;
    @JsonProperty("damageType")
    private List<String> damageTypes;
    @JsonProperty("healType")
    private List<String> healTypes;

    private Boolean homebrew;
    private Boolean settings;
}