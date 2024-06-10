package club.dnd5.portal.model.screen;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "screens")
public class Screen {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String altName;
	private String englishName;
	private String category;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "parent_id")
	private Screen parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = true)
	private List<Screen> chields;

	@Column(columnDefinition = "TEXT")
	private String description;
	private String icon;
	private int ordering;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
