package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FTarget {
    private Object value;
    private String width;
    private String units= "";
    private String type = "";
}
