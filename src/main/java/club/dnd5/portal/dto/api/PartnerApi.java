package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.Partner;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class PartnerApi {
	private String name;
	private String description;
	private String img;
	private String url;
	private int order;

	public PartnerApi(Partner partner) {
		name = partner.getName();
		img = partner.getImg();
		url = partner.getUrl();
		order = partner.getOrder();

		if (partner.getDescription() != null) {
			description = partner.getDescription();
		}
	}
}

