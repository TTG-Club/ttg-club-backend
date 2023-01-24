package club.dnd5.portal.model.foundary;

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
