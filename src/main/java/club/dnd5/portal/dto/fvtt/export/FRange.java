package club.dnd5.portal.dto.fvtt.export;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FRange {
    private String value;
    @JsonProperty("long")
    private String longer;
    private String units= "";
}
