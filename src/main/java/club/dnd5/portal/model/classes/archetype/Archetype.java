package club.dnd5.portal.model.classes.archetype;

import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.SpellLevelDefinition;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.util.StringUtils;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Entity
@Table(name = "archetypes")
@Getter
@Setter
public class Archetype {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String genitiveName;

	private String englishName;

	@Column(columnDefinition = "TEXT")
	private String description;

	private byte level;

	@ManyToOne(targetEntity = HeroClass.class)
	@JoinColumn(name = "class_id")
	private HeroClass heroClass;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private SpellcasterType spellcasterType;

	@OneToMany()
	@JoinColumn(name = "archetype_id")
	private List<SpellLevelDefinition> levelDefenitions;

	@OneToMany()
	@JoinColumn(name = "archetype_id")
	private List<FeatureLevelDefinition> featureLevelDefenitions;

	@OneToMany
	@JoinColumn(name = "archetype_id")
	private List<ArchetypeTrait> feats;

	@OneToMany
	@JoinColumn(name = "archetype_id")
	private List<ArchetypeSpell> spells;

	@Enumerated(EnumType.STRING)
	private Option.OptionType optionType;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public Map<Integer, List<ArchetypeSpell>> getLevelSpells(){
		return spells.stream().filter(s -> s.getLevel() > 0)
				.collect(Collectors.groupingBy(ArchetypeSpell::getLevel, TreeMap::new,
						Collectors.mapping(a -> a, Collectors.toList())));
	}

	public boolean isOfficial() {
		return book.getType() != null && book.getType() == TypeBook.OFFICAL;
	}

	public boolean isSetting() {
		return book.getType() != null && book.getType() == TypeBook.SETTING;
	}

	public String getCapitalizeName() {
		return StringUtils.capitalizeWords(name.toLowerCase());
	}

	public List<ArchetypeTrait> getFeats(){
		return feats.stream().sorted(Comparator.comparing(ArchetypeTrait::getLevel)).collect(Collectors.toList());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
