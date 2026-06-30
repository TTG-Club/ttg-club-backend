package club.dnd5.portal.dto.api.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.spells.SpellFilter;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.SpellLevelDefinition;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class ClassDetailApi extends ClassApi {
	private Collection<ClassTabAp> tabs = new ArrayList<>(5);
	private Collection<String> images;
	private SpellFilter customFilter;
	private String archetypeName;
	private ClassTraitsApi traits;
	private String description;
	private String accusativeName;
	private AbilityType spellAbility;
	private SpellcasterType spellcasterType;
	private Collection<AbilityType> primaryAbilities;
	private Collection<SkillType> availableSkillsRaw;
	private Option.OptionType optionType;
	private Rest slotsReset;
	private int enabledArhitypeLevel;
	private Short page;
	private Collection<ClassTraitApi> classTraits;
	private Collection<ArchetypeSpellLevelApi> archetypeSpells;

	public ClassDetailApi(HeroClass heroClass, Collection<String> images, ClassRequestApi request) {
		super(heroClass, request);
		this.images = images;
		description = heroClass.getDescription();
		accusativeName = heroClass.getAccusativeName();
		spellAbility = heroClass.getSpellAbility();
		spellcasterType = heroClass.getSpellcasterType();
		primaryAbilities = heroClass.getPrimaryAbilities() == null ? Collections.emptyList() : heroClass.getPrimaryAbilities();
		availableSkillsRaw = heroClass.getAvailableSkills() == null ? Collections.emptyList() : heroClass.getAvailableSkills();
		optionType = heroClass.getOptionType();
		slotsReset = heroClass.getSlotsReset();
		enabledArhitypeLevel = heroClass.getEnabledArhitypeLevel();
		page = heroClass.getPage();
		classTraits = heroClass.getTraits() == null
			? Collections.emptyList()
			: heroClass.getTraits()
				.stream()
				.filter(trait -> !trait.isArchitype())
				.map(ClassTraitApi::new)
				.collect(Collectors.toList());
		tabs.add(new ClassTabAp("Навыки", String.format("/classes/fragment/%s", heroClass.getUrlName()), "traits", 0, true));
		tabs.add(new ClassTabAp("Описание", String.format("/classes/%s/description", heroClass.getUrlName()), "description", 1, true));
		if (heroClass.getSpellcasterType() != null && heroClass.getSpellcasterType() != SpellcasterType.NONE) {
			tabs.add(new ClassTabAp("Заклинания", String.format("/filters/spells/%s", heroClass.getUrlName()), "spells", 2, false));
		}
		if (heroClass.getOptionType() != null) {
			tabs.add(new ClassTabAp(heroClass.getOptionType().getDisplayName(), String.format("/filters/options/%s", heroClass.getUrlName()), "options", 3, false));
		}
		if (heroClass.getArchetypeName() != null) {
			archetypeName = heroClass.getArchetypeName();
		}
		traits = new ClassTraitsApi(heroClass, null);
	}

	public ClassDetailApi(Archetype archetype, Collection<String> images, ClassRequestApi request) {
		super(archetype.getHeroClass(), request);
		name.setRus(name.getRus() + " " + archetype.getCapitalizeName());
		name.setEng(name.getEng() + " " + archetype.getEnglishName());
		HeroClass heroClass = archetype.getHeroClass();
		this.images = images;
		archetypeSpells = toArchetypeSpellLevels(archetype);
		tabs.add(new ClassTabAp("Навыки", String.format("/classes/%s/architypes/%s", heroClass.getUrlName(), archetype.getUrlName()), "traits", 0, true));
		tabs.add(new ClassTabAp("Описание", String.format("/classes/%s/archetype/%s/description", heroClass.getUrlName(), archetype.getUrlName()), "description", 1, true));
		if (heroClass.getSpellcasterType() != SpellcasterType.NONE || archetype.getSpellcasterType() != null) {
			tabs.add(new ClassTabAp("Заклинания", String.format("/filters/spells/%s/%s", heroClass.getUrlName(), archetype.getUrlName()), "spells", 2, false));
		}
		if (heroClass.getOptionType() != null) {
			tabs.add(new ClassTabAp(heroClass.getOptionType().getDisplayName(), String.format("/filters/options/%s", heroClass.getUrlName()), "options", 3, false));
		}
		if (archetype.getOptionType() != null) {
			tabs.add(new ClassTabAp(archetype.getOptionType().getDisplayName(), String.format("/filters/options/%s/%s", heroClass.getUrlName(), archetype.getUrlName()), "options", 4, false));
		}
		traits = new ClassTraitsApi(heroClass, archetype);
	}

	private Collection<ArchetypeSpellLevelApi> toArchetypeSpellLevels(Archetype archetype) {
		if (archetype.getSpells() == null) {
			return Collections.emptyList();
		}
		return archetype.getSpells()
			.stream()
			.filter(archetypeSpell -> archetypeSpell.getLevel() > 0 && archetypeSpell.getSpell() != null)
			.collect(Collectors.groupingBy(ArchetypeSpell::getLevel))
			.entrySet()
			.stream()
			.sorted(java.util.Map.Entry.comparingByKey())
			.map(entry -> new ArchetypeSpellLevelApi(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());
	}

	@Getter
	public static class ArchetypeSpellLevelApi {
		private final int level;
		private final Collection<ArchetypeSpellApi> spells;

		private ArchetypeSpellLevelApi(int level, Collection<ArchetypeSpell> spells) {
			this.level = level;
			this.spells = spells.stream()
				.sorted(Comparator.comparing(archetypeSpell -> archetypeSpell.getSpell().getName()))
				.map(ArchetypeSpellApi::new)
				.collect(Collectors.toList());
		}
	}

	@Getter
	public static class ArchetypeSpellApi {
		private final String name;
		private final String englishName;
		private final String url;
		private final String advanced;

		private ArchetypeSpellApi(ArchetypeSpell archetypeSpell) {
			Spell spell = archetypeSpell.getSpell();
			name = spell.getName();
			englishName = spell.getEnglishName();
			url = String.format("/spells/%s", spell.getUrlName());
			advanced = archetypeSpell.getAdvenced();
		}
	}

	@Getter
	public static class ClassTraitApi {
		private final Integer id;
		private final String name;
		private final String suffix;
		private final int level;
		private final String description;
		private final boolean optional;
		private final String child;

		private ClassTraitApi(HeroClassTrait trait) {
			id = trait.getId();
			name = trait.getName();
			suffix = trait.getSuffix();
			level = trait.getLevel();
			description = trait.getDescription();
			optional = trait.getOptional() == 1;
			child = trait.getChild();
		}
	}

	@Getter
	public static class ClassTraitsApi {
		private final int diceHp;
		private final String armor;
		private final String weapon;
		private final String tools;
		private final String savingThrows;
		private final int skillAvailableCount;
		private final Collection<String> availableSkills;
		private final String equipment;
		private final Collection<FeatureLevelApi> classFeatureLevels;
		private final Collection<FeatureLevelApi> archetypeFeatureLevels;
		private final Collection<LevelRowApi> levels;
		private final Collection<TraitApi> features;
		private final TraitApi archetype;

		private ClassTraitsApi(HeroClass heroClass, Archetype archetype) {
			diceHp = heroClass.getDiceHp();
			armor = heroClass.getArmor();
			weapon = heroClass.getWeapon();
			tools = heroClass.getTools();
			savingThrows = heroClass.getSavingThrows();
			skillAvailableCount = heroClass.getSkillAvailableCount();
			availableSkills = heroClass.getAvailableSkills() == null
				? Collections.emptyList()
				: heroClass.getAvailableSkills()
				.stream()
				.map(SkillType::getCyrilicName)
				.collect(Collectors.toList());
			equipment = heroClass.getEquipment();
			classFeatureLevels = toFeatureLevels(heroClass.getFeatureLevelDefenitions());
			archetypeFeatureLevels = archetype == null
				? Collections.emptyList()
				: toFeatureLevels(archetype.getFeatureLevelDefenitions());
			this.archetype = archetype == null ? null : new TraitApi(archetype);
			levels = IntStream.rangeClosed(1, 20)
				.mapToObj(level -> new LevelRowApi(heroClass, archetype, level))
				.collect(Collectors.toList());
			features = buildFeatures(heroClass, archetype);
		}

		private Collection<FeatureLevelApi> toFeatureLevels(List<FeatureLevelDefinition> definitions) {
			if (definitions == null) {
				return Collections.emptyList();
			}
			return definitions.stream().map(FeatureLevelApi::new).collect(Collectors.toList());
		}

		private Collection<TraitApi> buildFeatures(HeroClass heroClass, Archetype archetype) {
			Collection<TraitApi> result = new ArrayList<>();
			heroClass.getTraits()
				.stream()
				.filter(trait -> trait.getChild() == null)
				.forEach(trait -> result.add(new TraitApi(trait)));
			if (archetype != null) {
				archetype.getFeats()
					.stream()
					.filter(trait -> trait.getChild() == null)
					.forEach(trait -> result.add(new TraitApi(trait, archetype)));
			}
			return result.stream()
				.sorted(Comparator.comparingInt(TraitApi::getLevel))
				.collect(Collectors.toList());
		}
	}

	@Getter
	public static class FeatureLevelApi {
		private final String name;
		private final String prefix;
		private final String suffix;

		private FeatureLevelApi(FeatureLevelDefinition definition) {
			name = definition.getName();
			prefix = definition.getPrefix();
			suffix = definition.getSufix();
		}
	}

	@Getter
	public static class LevelRowApi {
		private final int level;
		private final int proficiencyBonus;
		private final Collection<TraitLinkApi> traits;
		private final Collection<String> classFeatureValues;
		private final Collection<String> archetypeFeatureValues;
		private final Collection<String> spellSlots;

		private LevelRowApi(HeroClass heroClass, Archetype archetype, int level) {
			this.level = level;
			proficiencyBonus = proficiencyBonus(level);
			traits = buildTraitLinks(heroClass, archetype, level);
			classFeatureValues = toFeatureValues(heroClass.getFeatureLevelDefenitions(), level);
			archetypeFeatureValues = archetype == null
				? Collections.emptyList()
				: toFeatureValues(archetype.getFeatureLevelDefenitions(), level);
			spellSlots = toSpellSlots(heroClass, archetype, level);
		}

		private int proficiencyBonus(int level) {
			if (level < 5) {
				return 2;
			}
			if (level < 9) {
				return 3;
			}
			if (level < 13) {
				return 4;
			}
			if (level < 17) {
				return 5;
			}
			return 6;
		}

		private Collection<TraitLinkApi> buildTraitLinks(HeroClass heroClass, Archetype archetype, int level) {
			Collection<TraitLinkApi> result = new ArrayList<>();
			if (archetype != null) {
				archetype.getFeats()
					.stream()
					.filter(trait -> trait.getLevel() == level)
					.sorted(Comparator.comparing(ArchetypeTrait::getName))
					.forEach(trait -> result.add(new TraitLinkApi(trait)));
			}
			heroClass.getTraits(level)
				.stream()
				.filter(trait -> !trait.isArchitype())
				.forEach(trait -> result.add(new TraitLinkApi(trait)));
			return result;
		}

		private Collection<String> toFeatureValues(List<FeatureLevelDefinition> definitions, int level) {
			if (definitions == null) {
				return Collections.emptyList();
			}
			return definitions.stream()
				.map(definition -> formatFeatureValue(definition, level))
				.collect(Collectors.toList());
		}

		private String formatFeatureValue(FeatureLevelDefinition definition, int level) {
			byte value = definition.getByLevel(level);
			if (value == 0) {
				return "—";
			}
			if (value == -1) {
				return "∞";
			}
			return String.format("%s%d%s",
				Objects.toString(definition.getPrefix(), ""),
				value,
				Objects.toString(definition.getSufix(), ""));
		}

		private Collection<String> toSpellSlots(HeroClass heroClass, Archetype archetype, int level) {
			if (archetype != null && archetype.getLevelDefenitions() != null && !archetype.getLevelDefenitions().isEmpty()) {
				return toSpellSlots(archetype.getLevelDefenitions(), level, SpellcasterType.PARTLY.getMaxSpellLevel());
			}
			if (heroClass.getSpellcasterType() == null || heroClass.getSpellcasterType() == SpellcasterType.NONE || heroClass.getSpellcasterType() == SpellcasterType.PARTLY) {
				return Collections.emptyList();
			}
			return toSpellSlots(heroClass.getLevelDefenitions(), level, heroClass.getSpellcasterType().getMaxSpellLevel());
		}

		private Collection<String> toSpellSlots(List<SpellLevelDefinition> definitions, int level, int maxSpellLevel) {
			if (definitions == null || definitions.size() < level) {
				return Collections.emptyList();
			}
			List<SpellLevelDefinition> sortedDefinitions = definitions.stream()
				.sorted(Comparator.comparing(SpellLevelDefinition::getLevel))
				.collect(Collectors.toList());
			SpellLevelDefinition definition = sortedDefinitions.get(level - 1);
			List<Byte> slots = Arrays.asList(
				definition.getSlot1(),
				definition.getSlot2(),
				definition.getSlot3(),
				definition.getSlot4(),
				definition.getSlot5(),
				definition.getSlot6(),
				definition.getSlot7(),
				definition.getSlot8(),
				definition.getSlot9());
			return slots.stream()
				.limit(maxSpellLevel)
				.map(slot -> slot == null || slot == 0 ? "—" : slot.toString())
				.collect(Collectors.toList());
		}
	}

	@Getter
	public static class TraitLinkApi {
		private final String name;
		private final String anchor;
		private final String tooltipUrl;
		private final boolean archetypeFeature;

		private TraitLinkApi(HeroClassTrait trait) {
			name = normalizeTraitName(trait.getName(), trait.getSuffix());
			String child = trait.getChild() == null ? trait.getId().toString() : trait.getChild();
			anchor = String.format("#c%s", child);
			tooltipUrl = String.format("/classes/feature/%d", trait.getId());
			archetypeFeature = false;
		}

		private TraitLinkApi(ArchetypeTrait trait) {
			name = normalizeTraitName(trait.getName(), trait.getSuffix());
			anchor = String.format("#a%d", trait.getId());
			tooltipUrl = String.format("/classes/archetype/feature/%d", trait.getId());
			archetypeFeature = true;
		}

		private static String normalizeTraitName(String name, String suffix) {
			return String.format("%s%s",
				Objects.toString(name, "").toLowerCase().trim(),
				Objects.toString(suffix, ""));
		}
	}

	@Getter
	public static class TraitApi {
		private final String id;
		private final String name;
		private final int level;
		private final String type;
		private final String description;
		private final Source source;
		private final boolean optional;
		private final boolean archetypeFeature;
		private final boolean archetypeRoot;

		private TraitApi(HeroClassTrait trait) {
			id = String.format("c%d", trait.getId());
			name = trait.getName();
			level = trait.getLevel();
			type = String.format("%d-%s уровень", trait.getLevel(), levelSuffix(trait.getLevel()));
			description = trait.getDescription();
			source = new Source(trait.getBook());
			optional = trait.getOptional() == 1;
			archetypeFeature = false;
			archetypeRoot = false;
		}

		private TraitApi(Archetype archetype) {
			id = String.format("ad%d", archetype.getId());
			name = archetype.getCapitalizeName();
			level = archetype.getLevel();
			type = archetype.getHeroClass().getArchetypeName();
			description = archetype.getDescription();
			source = new Source(archetype.getBook());
			optional = false;
			archetypeFeature = true;
			archetypeRoot = true;
		}

		private TraitApi(ArchetypeTrait trait, Archetype archetype) {
			id = String.format("a%d", trait.getId());
			name = trait.getName();
			level = trait.getLevel();
			type = String.format("%d-%s уровень, умение %s", trait.getLevel(), levelSuffix(trait.getLevel()), archetype.getGenitiveName());
			description = trait.getDescription();
			source = new Source(trait.getBook());
			optional = false;
			archetypeFeature = true;
			archetypeRoot = false;
		}

		private static String levelSuffix(int level) {
			return level >= 5 && level <= 8 ? "го" : "й";
		}
	}

	@Getter
	public static class Source {
		private final String name;
		private final String shortName;

		private Source(Book book) {
			name = book == null ? null : book.getName();
			shortName = book == null ? null : book.getSource();
		}
	}
}
