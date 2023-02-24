package club.dnd5.portal.model;

import club.dnd5.portal.model.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "youtube_videos")
public class YoutubeVideo {
	@Id
	@Column(columnDefinition="varchar(12)")
	private String id;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	private boolean active;
	@Column(name = "`order`")
	private int order;
	@JoinColumn(updatable = false)
	private LocalDateTime created;

	public void setCreated(LocalDateTime created) {
		this.created = LocalDateTime.now();
	}

	public YoutubeVideo() {
		this.created = LocalDateTime.now();
	}
}
