package club.dnd5.portal.dto.api.menu;

import club.dnd5.portal.model.Menu;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MenuApi {
	private String name;
	private String icon;
	private String url;
	private Boolean onlyDev;
	private Boolean external;
	private List<MenuApi> children;

	public MenuApi(Menu menu) {
		name = menu.getName();

		if (menu.getIcon() != null) {
			icon = menu.getIcon();
		}

		if (menu.getUrl() != null) {
			url = menu.getUrl();
		}

		if (menu.isExternal()) {
			external = true;
		}

		if (menu.isOnlyDev()) {
			onlyDev = true;
		}

		if (!menu.getChildren().isEmpty()) {
			children = menu.getChildren()
				.stream()
				.map(MenuApi::new)
				.collect(Collectors.toList());
		}
	}
}
