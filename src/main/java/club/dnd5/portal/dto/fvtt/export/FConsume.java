package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FConsume {
    private String type = "";
    private String target;
    private String amount;
}
