package club.dnd5.portal.dto.api.spells;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.splells.MagicSchool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpellFilter {
	@JsonProperty("book")
	private Set<String> books;

	@JsonProperty("level")
	private Set<Integer> levels;

	@JsonProperty("class")
    private Set<String> myclass;

    @JsonProperty("school")
    private Set<MagicSchool> schools;

    private Set<String> timecast;
    private Set<String> distance;
    private Set<String> duration;

    private Set<String> components;
    private Set<String> ritual;
    private Set<String> concentration;
    @JsonProperty("damageType")
    private Set<DamageType> damageTypes;

    private Boolean homebrew;
    private Boolean settings;
}
