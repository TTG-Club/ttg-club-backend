package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.dto.api.RequestApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MagicItemRequestApi extends RequestApi {
    public MagicItemFilter filter;
}
