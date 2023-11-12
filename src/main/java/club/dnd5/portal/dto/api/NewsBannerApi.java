package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.NewsBanner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class NewsBannerApi {
	private String name;

	private String description;

	private String image;

	private String url;

	private boolean active;

	public NewsBannerApi (NewsBanner newsBanner) {
		 name = newsBanner.getName();
		 description = newsBanner.getDescription();
		 image = newsBanner.getImage();
		 url = newsBanner.getUrl();
		 active = newsBanner.isActive();
	}
}
