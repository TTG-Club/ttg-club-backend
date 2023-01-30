package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FUses {
    private Integer value;
    private String max = "";
    private String per;
	private String recovery = "";
}
