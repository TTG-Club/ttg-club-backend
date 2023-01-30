package club.dnd5.portal.dto.fvtt.export.system;

import club.dnd5.portal.dto.fvtt.export.FValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FRecharge extends FValue {
    public boolean charged;
}
