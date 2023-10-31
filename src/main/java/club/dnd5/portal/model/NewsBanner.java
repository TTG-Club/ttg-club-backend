package club.dnd5.portal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
public class NewsBanner {
	@Id
	private Integer id;

	private String title;

	private String text;

	private boolean active;

	private String image;

	private String url;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
