package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "options")
public class Option {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String altName;
	private String englishName;

	/** Типы списков, в которые входит особенность. Справочник типов — {@link OptionTypeDefinition}. */
	@ManyToMany
	@JoinTable(name = "option_types",
		joinColumns = @JoinColumn(name = "option_id"),
		inverseJoinColumns = @JoinColumn(name = "option_type"),
		foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
		inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	private List<OptionTypeDefinition> optionTypes;

	private String prerequisite;
	private Integer level;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
