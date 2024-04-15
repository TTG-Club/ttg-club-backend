package club.dnd5.portal.dto.api.bestiary.request;

import lombok.Data;

@Data
public class ActionDetailRequest extends DescriptionRequest {
	private String actionType;
}
