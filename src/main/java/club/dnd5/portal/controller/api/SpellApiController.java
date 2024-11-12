package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.spell.SpellDetailApi;
import club.dnd5.portal.dto.api.spell.SpellRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.HealType;
import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.TimeCast;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Tag(name = "Заклинания", description = "API по заклинаниям")
@RequiredArgsConstructor
@RestController
public class SpellApiController {
	private static final String[][] classesMap = {{"1", "Бард"}, {"2", "Волшебник"}, {"3", "Друид"},
		{"4", "Жрец"}, {"5", "Колдун"}, {"6", "Паладин"}, {"7", "Следопыт"}, {"8", "Чародей"},
		{"14", "Изобретатель"}};

	private final SpellRepository spellRepository;
	private final ClassRepository classRepository;
	private final ArchetypeSpellRepository archetypeSpellRepository;

	@Operation(summary = "Получение краткого списка заклинаний")
	@PostMapping(value = "/api/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SpellApi> getSpells(@RequestBody SpellRequestApi request) {
		Specification<Spell> specification = null;
		Optional<RequestApi> optionalRequest = Optional.of(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getLevels().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("level").in(request.getFilter().getLevels()));
			}
			if (!request.getFilter().getMyclass().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<HeroClass, Spell> join = root.join("heroClass", JoinType.LEFT);
					query.distinct(true);
					return cb.and(join.get("id").in(request.getFilter().getMyclass()));
				});
			}
			if (request.getFilter().getSchools() != null && !request.getFilter().getSchools().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
					specification, (root, query, cb) -> root.get("school").in(request.getFilter().getSchools().stream().map(MagicSchool::valueOf).collect(Collectors.toList())));
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getDamageTypes())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<DamageType, Spell> join = root.join("damageType", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getDamageTypes()
						.stream()
						.map(DamageType::valueOf)
						.collect(Collectors.toList()));
				});
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getHealTypes())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<HealType, Spell> join = root.join("healType", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getHealTypes()
							.stream()
							.map(HealType::valueOf)
							.collect(Collectors.toList()));
				});
			}
			if (request.getFilter().getRitual() != null && !request.getFilter().getRitual().isEmpty()) {
				if (request.getFilter().getRitual().contains("yes")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("ritual"), true));
				}
				if (request.getFilter().getRitual().contains("no")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("ritual"), false));
				}
			}
			if (request.getFilter().getConcentration() != null && !request.getFilter().getConcentration().isEmpty()) {
				if (request.getFilter().getConcentration().contains("yes")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("concentration"), true));
				}
				if (request.getFilter().getConcentration().contains("no")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("concentration"), false));
				}
			}
			if (request.getFilter().getComponents() != null && !request.getFilter().getComponents().isEmpty()) {
				if (request.getFilter().getComponents().contains("1")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("verbalComponent"), true));
				}
				if (request.getFilter().getComponents().contains("2")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("somaticComponent"), true));
				}
				if (request.getFilter().getComponents().contains("3")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("materialComponent"), true));
				}
				if (request.getFilter().getComponents().contains("4")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("consumable"), true));
				}
				if (request.getFilter().getComponents().contains("5")) {
					specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> cb.equal(root.get("consumable"), false));
				}
			}
			if (request.getFilter().getTimecast() != null && !request.getFilter().getTimecast().isEmpty()) {
				List<Specification<Spell>> timecastSpecifications = new ArrayList<>();
				for (String timecast : request.getFilter().getTimecast()) {
					String[] parts = timecast.split("\\s");
					int time = Integer.parseInt(parts[0]);
					TimeUnit unit = TimeUnit.valueOf(parts[1]);
					Specification<Spell> timecastSpecification = (root, query, cb) -> {
						Join<TimeCast, Spell> join = root.join("times", JoinType.INNER);
						query.distinct(true);
						return cb.and(
							cb.equal(join.get("number"), time),
							cb.equal(join.get("unit"), unit)
						);
					};
					timecastSpecifications.add(timecastSpecification);
				}
				Specification<Spell> combinedSpecification = SpecificationUtil.combineWithOr(timecastSpecifications);
				specification = SpecificationUtil.getAndSpecification(specification, combinedSpecification);
			}
			if (request.getFilter().getDistance() != null && !request.getFilter().getDistance().isEmpty()) {
				Specification<Spell> addSpec = null;
				for (String distance : request.getFilter().getDistance()) {
					addSpec = SpecificationUtil.getOrSpecification(addSpec,
						(root, query, cb) -> cb.equal(root.get("distance"), distance));
				}
				specification = SpecificationUtil.getAndSpecification(specification, addSpec);
			}
			if (request.getFilter().getDuration() != null && !request.getFilter().getDuration().isEmpty()) {
				Specification<Spell> addSpec = null;
				for (String duration : request.getFilter().getDuration()) {
					addSpec = SpecificationUtil.getOrSpecification(addSpec,
						(root, query, cb) -> cb.like(root.get("duration"), "%" + duration + "%"));
				}
				specification = SpecificationUtil.getAndSpecification(specification, addSpec);
			}
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return spellRepository.findAll(specification, pageable).toList()
			.stream()
			.map(SpellApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Получение заклинания по английскому названию")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Found the spell",
			content = {@Content(mediaType = "application/json",
				schema = @Schema(implementation = SpellDetailApi.class))}),
		@ApiResponse(responseCode = "400", description = "Invalid id supplied",
			content = @Content),
		@ApiResponse(responseCode = "404", description = "Spell not found",
			content = @Content)})
	@PostMapping(value = "/api/v1/spells/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SpellDetailApi> getSpell(@PathVariable String englishName) {
		Spell spell = spellRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		SpellDetailApi spellApi = new SpellDetailApi(spell);
		List<Archetype> archetypes = archetypeSpellRepository.findAllBySpell(spell.getId());
		if (!archetypes.isEmpty()) {
			spellApi.setSubclasses(archetypes.stream().map(ReferenceClassApi::new).collect(Collectors.toList()));
		}
		List<Race> races = spellRepository.findAllRaceBySpell(spell.getId());
		if (!races.isEmpty()) {
			spellApi.setRaces(races.stream().map(ReferenceClassApi::new).collect(Collectors.toList()));
		}
		return ResponseEntity.ok(spellApi);
	}

	@Operation(summary = "Фильтры для заклинаний")
	@PostMapping("/api/v1/filters/spells")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = spellRepository.findBook(typeBook);
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
					.map(book -> new FilterValueApi(book.getSource(), book.getSource(), Boolean.TRUE, book.getName()))
					.collect(Collectors.toList()));
				sources.add(filter);
			}
		}
		filters.setSources(sources);

		List<FilterApi> otherFilters = new ArrayList<>();

		otherFilters.add(getLevelsFilter(9));

		FilterApi spellClassFilter = new FilterApi("Классы", "class");
		spellClassFilter.setValues(IntStream.range(0, classesMap.length)
			.mapToObj(indexSpellClass -> new FilterValueApi(classesMap[indexSpellClass][1], classesMap[indexSpellClass][0]))
			.collect(Collectors.toList()));
		otherFilters.add(spellClassFilter);

		FilterApi schoolSpellFilter = new FilterApi("Школа", "school");
		schoolSpellFilter.setValues(
			Arrays.stream(MagicSchool.values())
				.map(school -> new FilterValueApi(school.getName(), school.name()))
				.collect(Collectors.toList()));
		otherFilters.add(getSchoolsFilter());

		FilterApi ritualFilter = new FilterApi("Ритуал", "ritual");
		List<FilterValueApi> values = new ArrayList<>(2);
		values.add(new FilterValueApi("да", "yes"));
		values.add(new FilterValueApi("нет", "no"));
		ritualFilter.setValues(values);
		otherFilters.add(ritualFilter);

		FilterApi concentrationFilter = new FilterApi("Концентрация", "concentration");
		values = new ArrayList<>(2);
		values.add(new FilterValueApi("требуется", "yes"));
		values.add(new FilterValueApi("не требуется", "no"));
		concentrationFilter.setValues(values);
		otherFilters.add(concentrationFilter);

		FilterApi damageTypeFilter = new FilterApi("Тип урона", "damageType");
		damageTypeFilter.setValues(
			DamageType.getSpellDamage().stream()
				.map(value -> new FilterValueApi(value.getCyrillicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(damageTypeFilter);
		FilterApi healTypeFilter = new FilterApi("Лечение", "healType");
		damageTypeFilter.setValues(Arrays.stream(HealType.values())
				.map(t -> new FilterValueApi(t.getName(), t.name()))
				.collect(Collectors.toList()));
		otherFilters.add(healTypeFilter);

		FilterApi timecastFilter = new FilterApi("Время накладывания", "timecast");
		values = new ArrayList<>();
		values.add(new FilterValueApi("бонусное действие", "1 BONUS"));
		values.add(new FilterValueApi("реакция", "1 REACTION"));
		values.add(new FilterValueApi("действие", "1 ACTION"));
		values.add(new FilterValueApi("ход", "1 ROUND"));
		values.add(new FilterValueApi("1 минута", "1 MINUTE"));
		values.add(new FilterValueApi("10 минут", "10 MINUTE"));
		values.add(new FilterValueApi("1 час", "1 HOUR"));
		values.add(new FilterValueApi("8 час", "8 HOUR"));
		values.add(new FilterValueApi("12 час", "12 HOUR"));
		values.add(new FilterValueApi("24 час", "24 HOUR"));
		timecastFilter.setValues(values);
		otherFilters.add(timecastFilter);

		FilterApi distanceFilter = new FilterApi("Дистанция", "distance");
		values = new ArrayList<>();
		values.add(new FilterValueApi("на себя", "На себя"));
		values.add(new FilterValueApi("касание", "Касание"));
		values.add(new FilterValueApi("5 футов", "5 футов"));
		values.add(new FilterValueApi("10 футов", "10 футов"));
		values.add(new FilterValueApi("20 футов", "20 футов"));
		values.add(new FilterValueApi("25 футов", "25 футов"));
		values.add(new FilterValueApi("30 футов", "30 футов"));
		values.add(new FilterValueApi("40 футов", "40 футов"));
		values.add(new FilterValueApi("50 футов", "50 футов"));
		values.add(new FilterValueApi("60 футов", "60 футов"));
		values.add(new FilterValueApi("90 футов", "90 футов"));
		values.add(new FilterValueApi("100 футов", "100 футов"));
		values.add(new FilterValueApi("120 футов", "120 футов"));
		values.add(new FilterValueApi("150 футов", "150 футов"));
		values.add(new FilterValueApi("300 футов", "300 футов"));
		values.add(new FilterValueApi("400 футов", "400 футов"));
		values.add(new FilterValueApi("1000 футов", "1000 футов"));
		values.add(new FilterValueApi("1 миля", "1 миля"));
		values.add(new FilterValueApi("500 миль", "500 миль"));
		distanceFilter.setValues(values);
		otherFilters.add(distanceFilter);

		FilterApi durationFilter = new FilterApi("Длительность", "duration");
		values = new ArrayList<>();
		values.add(new FilterValueApi("Мгновенная", "Мгновенная"));
		values.add(new FilterValueApi("1 раунд", "1 раунд"));
		values.add(new FilterValueApi("1 минута", "1 минута"));
		values.add(new FilterValueApi("10 минут", "10 минут"));
		values.add(new FilterValueApi("1 час", "1 час"));
		values.add(new FilterValueApi("8 часов", "8 часов"));
		values.add(new FilterValueApi("12 часов", "12 часов"));
		values.add(new FilterValueApi("24 часа", "24 часа"));
		values.add(new FilterValueApi("1 день", "1 день"));
		values.add(new FilterValueApi("7 дней", "7 дней"));
		values.add(new FilterValueApi("10 дней", "10 дней"));
		values.add(new FilterValueApi("1 год", "1 год"));
		durationFilter.setValues(values);
		otherFilters.add(durationFilter);

		otherFilters.add(getComponentsFilter());

		filters.setOther(otherFilters);
		return filters;
	}

	@Operation(summary = "Получение фильтров заклинаний для класса")
	@PostMapping("/api/v1/filters/spells/{englishClassName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		if (heroClass.getEnglishName().equals("Warlock")) {
			otherFilters.add(getLevelsFilter(Spell.MAX_LEVEL));
		} else {
			otherFilters.add(getLevelsFilter(heroClass.getSpellcasterType().getMaxSpellLevel()));
		}
		otherFilters.add(getComponentsFilter());
		otherFilters.add(getSchoolsFilter());

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классы");
		customFilter.setKey("class");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(String.valueOf(heroClass.getId()));
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	@Operation(summary = "Получение фильтров заклинаний для архетипа")
	@PostMapping("/api/v1/filters/spells/{englishClassName}/{englishArchetypeName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName, @PathVariable String englishArchetypeName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		if (heroClass.getEnglishName().equals("Warlock")) {
			otherFilters.add(getLevelsFilter(Spell.MAX_LEVEL));
		} else {
			otherFilters.add(getLevelsFilter(heroClass.getSpellcasterType().getMaxSpellLevel()));
		}
		otherFilters.add(getComponentsFilter());
		otherFilters.add(getSchoolsFilter());

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классы");
		customFilter.setKey("class");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		if ("Eldritch_Knight".equalsIgnoreCase(englishArchetypeName) ||
			"Arcane_Trickster".equalsIgnoreCase(englishArchetypeName)) {
			customValue.setKey(String.valueOf(2));
		} else {
			customValue.setKey(String.valueOf(heroClass.getId()));
		}
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);

		return filters;
	}

	private FilterApi getLevelsFilter(int maxLevel) {
		FilterApi levelFilter = new FilterApi("Уровень", "level");
		levelFilter.setValues(IntStream.rangeClosed(0, maxLevel)
			.mapToObj(level -> new FilterValueApi(level == 0 ? "заговор" : String.valueOf(level), String.valueOf(level)))
			.collect(Collectors.toList()));
		return levelFilter;
	}

	private FilterApi getSchoolsFilter() {
		FilterApi schoolSpellFilter = new FilterApi("Школа", "school");
		schoolSpellFilter.setValues(
			Arrays.stream(MagicSchool.values())
				.map(school -> new FilterValueApi(school.getName(), school.name()))
				.collect(Collectors.toList()));
		return schoolSpellFilter;
	}

	private FilterApi getComponentsFilter() {
		FilterApi componentsSpellFilter = new FilterApi("Компоненты", "components");
		List<FilterValueApi> componentValues = new ArrayList<>();
		componentValues.add(new FilterValueApi("вербальный", "1"));
		componentValues.add(new FilterValueApi("соматический", "2"));
		componentValues.add(new FilterValueApi("материальный", "3"));
		componentValues.add(new FilterValueApi("расходуемый", "4"));
		componentValues.add(new FilterValueApi("не расходуемый", "5"));

		componentsSpellFilter.setValues(componentValues);
		return componentsSpellFilter;
	}
}
