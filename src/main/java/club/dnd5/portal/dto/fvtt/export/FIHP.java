package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FIHP {
    private byte value = 0;
    private byte  max = 0;
    private String dt;
    private String conditions = "";
}
