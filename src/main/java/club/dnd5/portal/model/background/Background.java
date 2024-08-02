package club.dnd5.portal.model.background;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "backgrounds")
public class Background extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	@Column(columnDefinition = "TEXT")
	private String toolOwnership;

	@Column(columnDefinition = "TEXT")
	private String equipmentsText;

	@ElementCollection(targetClass = SkillType.class)
	@CollectionTable(name = "background_skill_type")
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Set<SkillType> skills;

	@ElementCollection(targetClass = AbilityType.class)
	@CollectionTable(name = "background_abilitity_types")
	@Enumerated(EnumType.STRING)
	private Set<AbilityType> abilities;

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

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;
}