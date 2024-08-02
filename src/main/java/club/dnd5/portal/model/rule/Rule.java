package club.dnd5.portal.model.rule;

import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter

@Entity
@Table(name = "rules")
public class Rule extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	private String type;

	@Column(columnDefinition = "TEXT")
	private String description;
	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

}
