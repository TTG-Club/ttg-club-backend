package club.dnd5.portal.dto.fvtt.export;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FActivation {
    private String type = "";
    private byte cost;
    private String condition = "";
}
