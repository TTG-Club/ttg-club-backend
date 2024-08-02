package club.dnd5.portal.model.screen;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
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
public class Screen extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

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
}
