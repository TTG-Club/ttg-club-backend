package club.dnd5.portal.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "info_pages")
public class InfoPage {
	@Id
	@Column(name = "url", nullable = false)
	private String url;
	private String title;
	private String subtitle;
	private String englishName;
	@Column(columnDefinition = "TEXT")
	private String description;
}
