package club.dnd5.portal.model.background;

import java.util.ArrayList;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "backgrounds")
public class Background {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String englishName;
	private String altName;

	@Column(columnDefinition = "TEXT")
	private String toolOwnership;

	@Column(columnDefinition = "TEXT")
	private String equipmentsText;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0017
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<SkillType> skills = new ArrayList<>();

	@Column(columnDefinition = "TEXT")
	private String otherSkills;

	private String skillName;

	@Column(columnDefinition = "TEXT")
	private String skillDescription;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String language;

	@ManyToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Language> languages;

	private int startMoney;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "background_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<Personalization> personalizations;

	@Column(columnDefinition = "TEXT")
	private String personalization;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private LifeStyle lifeStyle;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getCapitalazeName() {
		return StringUtil.capitalizeWords(name.toLowerCase());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}

	/** jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullJsonListsToEmpty() {
		skills = skills == null ? new ArrayList<>() : skills;
	}
}
