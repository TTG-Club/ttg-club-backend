package club.dnd5.portal.dto.fvtt.export.system;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FMaterials {
    public String value;
    public boolean consumed;
    public int cost;
    public int supply;
}
