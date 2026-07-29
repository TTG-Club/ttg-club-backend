package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.screen.Screen;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class ScreenSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private String category;
	private String description;
	private String icon;
	private Integer order;
	private String source;
	private Integer parentId;

	public ScreenSaveApi(Screen screen) {
		name = screen.getName();
		englishName = screen.getEnglishName();
		altName = screen.getAltName();
		category = screen.getCategory();
		description = screen.getDescription();
		icon = screen.getIcon();
		order = screen.getOrdering();
		source = screen.getBook() == null ? null : screen.getBook().getSource();
		parentId = screen.getParent() == null ? null : screen.getParent().getId();
	}
}
