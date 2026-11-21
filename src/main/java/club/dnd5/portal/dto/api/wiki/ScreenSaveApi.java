package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

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
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
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
