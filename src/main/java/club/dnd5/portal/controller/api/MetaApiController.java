package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.MetaApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.InfoPage;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.Option.OptionType;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.trait.Trait;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.InfoPagesRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Метаданные", description = "API для получения метаданных по странице")
@RequiredArgsConstructor
@RestController
public class MetaApiController {
	private final ImageRepository imageRepository;
	private final ClassRepository classRepository;
	private final RaceRepository raceRepository;
	private final TraitRepository traitRepository;
	private final BackgroundRepository backgroundRepository;
	private final SpellRepository spellRepository;
	private final OptionRepository optionRepository;
	private final WeaponRepository weaponRepository;
	private final ArmorRepository armorRepository;
	private final ItemRepository itemRepository;
	private final MagicItemRepository magicItemRepository;
	private final BestiaryRepository bestiaryItemRepository;
	private final ScreenRepository screenRepository;
	private final GodRepository godRepository;
	private final RuleRepository ruleRepository;
	private final BookRepository bookRepository;
	private final InfoPagesRepository infoPagesRepository;

	@GetMapping(value = "/api/v1/meta/*", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getNotMapping() {
		MetaApi meta = new MetaApi();
		meta.setTitle("TTG Club Oнлайн-справочник");
		meta.setDescription("TTG.Club - сайт, посвященный DnD 5-й редакции. Тут можно найти: расы, происхождения, классы, заклинания, бестиарий, снаряжение, магические предметы и инструменты для облегчения игры как игрокам, так и мастерам - все в одном месте.");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/classes", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getClassesMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Классы (Classes) D&D 5e");
		meta.setMenu("Классы");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/classes/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getClassMeta(@PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Классы D&D 5e", heroClass.getName(), heroClass.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - описание класса персонажа по D&D 5-редакции", heroClass.getCapitalazeName(), heroClass.getEnglishName()));
		meta.setMenu("Классы");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/classes/{classEnglishName}/{archetypeEnglishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getArchetypeMeta(@PathVariable String classEnglishName, @PathVariable String archetypeEnglishName) {
		HeroClass heroClass = classRepository.findByEnglishName(classEnglishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		Archetype archetype = heroClass.getArchetypes().stream()
				.filter(a -> a.getEnglishName().equalsIgnoreCase(archetypeEnglishName.replace('_', ' ')))
				.findFirst().orElseThrow(PageNotFoundException::new);
		meta.setTitle(String.format("%s - %s (%s) | Классы | Подклассы D&D 5e",
				StringUtils.capitalize(archetype.getName().toLowerCase()), heroClass.getName(), heroClass.getEnglishName()));
		meta.setDescription(String.format("%s - описание %s класса %s из D&D 5 редакции",
				archetype.getName(), heroClass.getArchetypeName(), heroClass.getName()));
		meta.setMenu("Классы");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/races", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getRacesMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Расы и происхождения (Races) D&D 5e");
		meta.setMenu("Расы и происхождения");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/races/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getRaceMeta(@PathVariable String englishName) {
		Race race = raceRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(race.getName() + " | Расы и происхождения D&D 5e");
		meta.setDescription(String.format("%s - раса персонажа по D&D 5 редакции", race.getName()));
		meta.setMenu("Расы и происхождения");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/races/{englishName}/{subrace}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MetaApi> getSubraceMeta(@PathVariable String englishName, @PathVariable String subrace) {
		Race race = raceRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		Race subRace = race.getSubRaces()
			.stream()
			.filter(r -> r.getEnglishName().equalsIgnoreCase(subrace.replace('_', ' ')))
			.findFirst()
			.orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s | Расы и происхождения | Разновидности D&D 5e", subRace.getName()));
		meta.setDescription(String.format("%s - разновидность расы персонажа по D&D 5 редакции", subRace.getName()));
		meta.setMenu("Расы и происхождения");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, subRace.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		return ResponseEntity.ok(meta);
	}

	@GetMapping(value = "/api/v1/meta/traits", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getTraitsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Черты (Traits) D&D 5e");
		meta.setDescription("Черты по D&D 5 редакции");
		meta.setMenu("Черты");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/traits/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getTraitMeta(@PathVariable String englishName) {
		Trait trait = traitRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle( String.format("%s (%s)", trait.getName(), trait.getEnglishName()) + " | Черты D&D 5e");
		meta.setDescription(String.format("%s (%s) - черта персонажа по D&D 5-редакции", trait.getName(), trait.getEnglishName()));
		meta.setMenu("Черты");
		meta.setKeywords(trait.getAltName() + " " + trait.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/backgrounds", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBackgroundsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Предыстории персонажей (Backgrounds) D&D 5e");
		meta.setDescription("Предыстории по D&D 5 редакции");
		meta.setMenu("Предыстории");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/backgrounds/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBackgroundMeta(@PathVariable String englishName) {
		Background background = backgroundRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(background.getName() + " | Предыстории персонажей D&D 5e");
		meta.setDescription(String.format("%s (%s) - предыстория персонажа по D&D 5 редакции", background.getName(), background.getEnglishName()));
		meta.setMenu("Предыстории");
		meta.setKeywords(background.getAltName() + " " + background.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/options", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getOptionsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Особенности классов (Options) D&D 5e");
		meta.setDescription("Особенности классов по D&D 5 редакции");
		meta.setMenu("Особенности классов");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/options/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getOptiondMeta(@PathVariable String englishName) {
		Option option = optionRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s)", option.getName(), option.getEnglishName()) + " | Особенности классов D&D 5e");
		meta.setDescription(
				String.format("Описание особенности %s - %s",
						option.getOptionTypes().stream().map(OptionType::getDisplayName).collect(Collectors.joining()),
						option.getName()));
		meta.setMenu("Особенности классов");
		meta.setKeywords(option.getAltName() + " " + option.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getSpellsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Заклинания (Spells) D&D 5e");
		meta.setDescription("Заклинания по D&D 5 редакции");
		meta.setMenu("Заклинания");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/spells/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getSpellMeta(@PathVariable String englishName) {
		Spell spell = spellRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s)", spell.getName(), spell.getEnglishName()) + " | Заклинания D&D 5e");
		meta.setDescription(String.format("%s %s, %s", (spell.getLevel() == 0 ? "Заговор" : spell.getLevel() + " уровень"), spell.getName(), spell.getSchool().getName()));
		meta.setMenu("Заклинания");
		meta.setImage(String.format("https://img.ttg.club/magic/%s.png", StringUtils.capitalize(spell.getSchool().name().toLowerCase())));
		meta.setKeywords(spell.getAltName() + " " + spell.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/weapons", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getWeaponsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Оружие (Weapons) D&D 5e");
		meta.setDescription("Оружие по D&D 5 редакции");
		meta.setMenu("Оружие");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/weapons/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getWeaponMeta(@PathVariable String englishName) {
		Weapon weapon = weaponRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Оружие D&D 5e", weapon.getName(), weapon.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - %s D&D 5 редакции", weapon.getName(), weapon.getEnglishName(), weapon.getType().getName()));
		meta.setMenu("Оружие");
		meta.setKeywords(weapon.getAltName() + " " + weapon.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/armors", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getArmorsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Доспехи (Armors) D&D 5e");
		meta.setDescription("Доспехи по D&D 5 редакции");
		meta.setMenu("Доспехи");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/armors/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getArmorMeta(@PathVariable String englishName) {
		Armor armor = armorRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Доспехи D&D 5e", armor.getName(), armor.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - доспехи по D&D 5 редакции", armor.getName(), armor.getEnglishName()));
		meta.setMenu("Доспехи");
		meta.setKeywords(armor.getAltName() + " " + armor.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getItemsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Снаряжение (Items) D&D 5e");
		meta.setDescription("Снаряжение, инструменты и транспорт по D&D 5 редакции");
		meta.setMenu("Снаряжение");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/items/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getItemMeta(@PathVariable String englishName) {
		Equipment item = itemRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Снаряжение D&D 5e",item.getName(), item.getEnglishName()));
		meta.setDescription(String.format("%s (%s) снаряжение по D&D 5 редакции", item.getName(), item.getEnglishName()));
		meta.setMenu("Снаряжение");
		meta.setKeywords(item.getAltName() + " " + item.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/items/magic", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getMagicItemsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Магические предметы (Magic items) D&D 5e");
		meta.setDescription("Магические предметы и артефакты по D&D 5 редакции");
		meta.setMenu("Магические предметы");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/items/magic/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getMagicItemMeta(@PathVariable String englishName) {
		MagicItem item = magicItemRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Магические предметы D&D 5e", item.getName(), item.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - %s %s", item.getName(), item.getEnglishName(), item.getTextRarity(), item.getType().getCyrilicName()));
		meta.setMenu("Магические предметы");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.MAGIC_ITEM, item.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		meta.setKeywords(item.getAltName() + " " + item.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/bestiary", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBeastsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Бестиарий (Monster Manual) D&D 5e");
		meta.setDescription("Бестиарий - существа для D&D 5 редакции");
		meta.setMenu("Бестиарий");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/bestiary/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBeastMeta(@PathVariable String englishName) {
		List<Creature> beasts = bestiaryItemRepository.findByEnglishName(englishName.replace('_', ' '));
		if (beasts.isEmpty()) {
			throw new PageNotFoundException();
		}
		Creature beast = beasts.get(0);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Бестиарий D&D 5e", beast.getName(), beast.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - %s %s, %s с уровнем опасности %s",
			beast.getName(),
			beast.getEnglishName(),
			beast.getSizeName(),
			beast.getType().getCyrilicName(),
			beast.getAligment(),
			beast.getChallengeRating()));
		meta.setMenu("Бестиарий");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CREATURE, beast.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		meta.setKeywords(beast.getAltName() + " " + beast.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/screens", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getScreensMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Ширма Мастера (Screens) D&D 5e");
		meta.setDescription("Ширма Мастера Подземелий и Драконов по D&D 5 редакции");
		meta.setMenu("Ширма");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/screens/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getScreenMeta(@PathVariable String englishName) {
		Screen screen = screenRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Ширма Мастера (Screens) D&D 5e", screen.getName(), screen.getEnglishName()));
		meta.setDescription(String.format("%s (%s) Ширма Мастера Подземелий и Драконов по D&D 5 редакции",
			screen.getName(), screen.getEnglishName()));
		meta.setMenu("Ширма");
		meta.setKeywords(screen.getAltName() + " " + screen.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/gods", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getGodsMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Боги (Gods) D&D 5e");
		meta.setDescription("Боги, полубоги и философии D&D 5 редакции");
		meta.setMenu("Боги");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/gods/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getGodMeta(@PathVariable String englishName) {
		God god = godRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Боги D&D 5e", god.getName(), god.getEnglishName()));
		meta.setDescription(String.format("%s (%s) - %s %s, %s", god.getName(), god.getEnglishName(), god.getAligment().getCyrilicName(), god.getSex().getCyrilicName(), god.getCommitment()));
		meta.setMenu("Боги");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.GOD, god.getId());
		if (!images.isEmpty()) {
			meta.setImage(images.iterator().next());
		}
		meta.setKeywords(god.getAltName() + " " + god.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getRulesMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Правила и термины [Rules] D&D 5e");
		meta.setDescription("Правила и термины [Rules] D&D 5e");
		meta.setMenu("Правила и термины");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/rules/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getRuleMeta(@PathVariable String englishName) {
		Rule rule = ruleRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s | %s | Правила и термины [Rules] D&D 5e", rule.getName(), rule.getType()));
		meta.setDescription(String.format("%s (%s) Правила и термины по D&D 5 редакции", rule.getName(), rule.getEnglishName()));
		meta.setMenu("Правила и термины");
		meta.setKeywords(rule.getAltName() + " " + rule.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/books", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBooksMeta() {
		MetaApi meta = new MetaApi();
		meta.setTitle("Источники (Sources) D&D 5e");
		meta.setDescription("Источники [Sources] D&D 5e");
		meta.setMenu("Источники");
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/books/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getBooksMeta(@PathVariable String englishName) {
		Book book = bookRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s (%s) | Источники (Books) D&D 5e", book.getName(), book.getEnglishName()));
		meta.setDescription(String.format("%s (%s) Источник [Source] по D&D 5 редакции", book.getName(), book.getEnglishName()));
		meta.setMenu("Источники");
		meta.setKeywords(book.getAltName() + " " + book.getEnglishName());
		return meta;
	}

	@GetMapping(value = "/api/v1/meta/info/{url}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetaApi getInfoMeta(@PathVariable String url) {
		InfoPage infoPage = infoPagesRepository.findByUrl(url);
		MetaApi meta = new MetaApi();
		meta.setTitle(String.format("%s | TTG Club", infoPage.getTitle()));
		meta.setDescription(infoPage.getSubtitle());
		return meta;
	}
}
