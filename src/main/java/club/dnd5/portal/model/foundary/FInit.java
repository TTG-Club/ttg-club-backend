package club.dnd5.portal.model.foundary;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@JsonInclude(JsonInclude.Include.NON_NULL)


@Getter
@Setter
public class FInit {
	private String ability = "";
	private int bonus = 0;
    private Integer value;
    private Integer mod;
    private Integer total;
    private Integer prof;
}
