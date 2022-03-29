package club.dnd5.portal.dto.api.spells;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class Components {
    public Boolean v;
    public Boolean s;
    public String m;
}