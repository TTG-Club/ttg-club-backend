package club.dnd5.portal.model.rule;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "rules")
public class Rule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String altName;
	private String englishName;
	private String type;

	/** Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0025/0026). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();

	/** jsonb-колонка NOT NULL: «нет описания» хранится как [], а не NULL. */
	@PrePersist
	@PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
	}
	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
