package club.dnd5.portal.model.trait;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "traits")
public class Trait extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

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
}
