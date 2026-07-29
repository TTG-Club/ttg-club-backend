package club.dnd5.portal.dto.api.wiki;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.screen.Screen;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class ScreenDetailApi extends ScreenApi {
	private Integer id;
	private String altName;
	private String category;
    private String description;
    private ScreenDetailApi parent;
    private List<ScreenApi> chields;

    public ScreenDetailApi(Screen screen) {
		id = screen.getId();
		altName = screen.getAltName();
		category = screen.getCategory();
        name = new NameApi(screen.getName(), screen.getEnglishName());
        url = String.format("/screens/%s", screen.getUrlName());
        order = screen.getOrdering();
		setIcon(screen.getIcon());
		setGroup(screen.getCategory());
		if (Objects.nonNull(screen.getBook())) {
			source = new SourceApi(screen.getBook());
		}

        if (Objects.nonNull(screen.getParent())) {
            description = screen.getDescription();
            parent = new ScreenDetailApi(screen.getParent());
            parent.setDescription(null);
            parent.setChields(null);
        } else {
			if (Objects.nonNull(screen.getDescription())) {
				description = screen.getDescription();
			}
            chields = screen.getChields()
                    .stream()
                    .map(ScreenApi::new)
                    .collect(Collectors.toList());
        }
    }
}
