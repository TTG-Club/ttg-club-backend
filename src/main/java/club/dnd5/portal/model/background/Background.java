package club.dnd5.portal.model.background;

import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thymeleaf.util.StringUtils;

import javax.persistence.*;
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

	@ElementCollection(targetClass = SkillType.class)
	@CollectionTable(name = "background_skill_type")
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private List<SkillType> skills;

	@Column(columnDefinition = "TEXT")
	private String otherSkills;

	private String skillName;

	@Column(columnDefinition = "TEXT")
	private String skillDescription;

	@Column(columnDefinition = "TEXT")
	private String description;

	private String language;

	@ManyToMany
	private List<Language> languages;

	private int startMoney;

	@OneToMany
	@JoinColumn(name = "background_id")
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
		return StringUtils.capitalizeWords(name.toLowerCase());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
