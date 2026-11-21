package club.dnd5.portal.model.trait;

import java.util.ArrayList;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "traits")
public class Trait {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String englishName;
	private String altName;
	private Integer level;
	private String requirement;

	/** Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0021/0022). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0017
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<AbilityType> abilities = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0017
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<SkillType> skills = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
	public String getCapitalazeName() {
		return StringUtils.capitalize(name.toLowerCase());
	}

	/** jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullJsonListsToEmpty() {
		abilities = abilities == null ? new ArrayList<>() : abilities;
		skills = skills == null ? new ArrayList<>() : skills;
		description = description == null ? new ArrayList<>() : description;
	}
}
