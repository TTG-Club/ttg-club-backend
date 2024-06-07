package club.dnd5.portal.model.trait;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
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

	@Column(columnDefinition = "TEXT")
	private String description;

	@ElementCollection(targetClass = AbilityType.class)
	@JoinTable(name = "trait_abilities", joinColumns = @JoinColumn(name = "trait_id"))
	@Column(name = "ability", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<AbilityType> abilities;

	@ElementCollection(targetClass = SkillType.class)
	@JoinTable(name = "trait_skills", joinColumns = @JoinColumn(name = "trait_id"))
	@Column(name = "skill", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<SkillType> skills;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
	public String getCapitalazeName() {
		return StringUtils.capitalize(name.toLowerCase());
	}
}
