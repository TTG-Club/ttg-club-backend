package club.dnd5.portal.dto.api.youtube;

import club.dnd5.portal.model.YoutubeVideo;
import club.dnd5.portal.model.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class YoutubeVideoApi {
	@NotNull
	private String id;
	@NotNull
	private String name;
	private User user;
	private boolean active;
	private int order;
	private LocalDateTime created;

	public YoutubeVideoApi(YoutubeVideo video) {
		id = video.getId();
		name = video.getName();
		active = video.isActive();
		order = video.getOrder();
		created = video.getCreated();
	}
}
