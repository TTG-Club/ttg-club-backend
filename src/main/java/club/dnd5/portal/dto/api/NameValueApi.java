package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.AbilityType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Builder
@Getter
@Setter
public class NameValueApi {
	private AbilityType key;
	private String name;
	private String shortName;
	private Object value;
	private Object additional;
}
