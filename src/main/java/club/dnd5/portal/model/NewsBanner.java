package club.dnd5.portal.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class NewsBanner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String description;

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
