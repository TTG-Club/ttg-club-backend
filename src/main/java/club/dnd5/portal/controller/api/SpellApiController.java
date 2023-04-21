package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.spell.SpellDetailApi;
import club.dnd5.portal.dto.api.spell.SpellRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.spells.SpellFilter;
import club.dnd5.portal.dto.api.spells.SpellFvtt;
import club.dnd5.portal.dto.api.spells.SpellsFvtt;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.SpellTag;
import club.dnd5.portal.model.splells.TimeCast;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Tag(name = "Spell", description = "The Spell API")
@PropertySource("spell.properties")
@RestController
public class SpellApiController {
	private static final String[][] classesMap = { { "1", "Бард" }, { "2", "Волшебник" }, { "3", "Друид" },
			{ "4", "Жрец" }, { "5", "Колдун" }, { "6", "Паладин" }, { "7", "Следопыт" }, { "8", "Чародей" },
			{ "14", "Изобретатель" } };

	@Autowired
	private SpellRepository spellRepository;
	@Autowired
	private ClassRepository classRepository;
	@Autowired
	private ArchetypeSpellRepository archetypeSpellRepository;
	@Autowired
	private Environment env;
	private List<FilterValueApi> timecasts;
	private List<FilterValueApi> distancies;
	@PostConstruct
	public void init() {
		timecasts = new ArrayList<>();
		String[] names = env.getProperty("spell.time-cast.names").split(",");
		String[] englishNames = env.getProperty("spell.time-cast.names.english").split(",");
		for (int i = 0; i < names.length; i++) {
			timecasts.add(FilterValueApi
				.builder()
				.label(names[i])
				.key(englishNames[i])
				.build()
			);
		}
		names = env.getProperty("spell.distance.names").split(",");
		distancies = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			distancies.add(FilterValueApi
				.builder()
				.label(names[i])
				.key(names[i])
				.build()
			);
		}
	}

	@Operation(summary = "Gets all spells")
	@PostMapping(value = "/api/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<SpellApi> getSpells(@RequestBody SpellRequesApi request) {
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Specification<Spell> specification = null;
		Optional<SpellRequesApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			if (optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
			} else {
				String likeSearch = "%" + request.getSearch().getValue() + "%";
				specification = (root, query, cb) -> cb.or(cb.like(root.get("altName"), likeSearch),
					cb.like(root.get("englishName"), likeSearch),
					cb.like(root.get("name"), likeSearch));
			}
		}
		Optional<SpellFilter> filter = optionalRequest.map(SpellRequesApi::getFilter);
		if (filter.isPresent()) {
			if (!filter.map(SpellFilter::getLevels).orElse(Collections.emptyList()).isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("level").in(request.getFilter().getLevels()));
			}
			if (!filter.map(SpellFilter::getMyclass).orElse(Collections.emptyList()).isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<HeroClass, Spell> join = root.join("heroClass", JoinType.LEFT);
					query.distinct(true);
					return cb.and(join.get("id").in(request.getFilter().getMyclass()));
				});
			}
			if (!filter.map(SpellFilter::getSchools).orElse(Collections.emptyList()).isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
					specification, (root, query, cb) -> root.get("school").in(request.getFilter().getSchools().stream()
						.map(MagicSchool::valueOf)
						.collect(Collectors.toList())));
			}
			if (!filter.map(SpellFilter::getDamageTypes).orElse(Collections.emptyList()).isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<DamageType, Spell> join = root.join("damageType", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getDamageTypes()
						.stream()
						.map(DamageType::valueOf)
						.collect(Collectors.toList()));
				});
			}
			if (!filter.map(SpellFilter::getRitual).orElse(Collections.emptyList()).isEmpty()) {
				if(request.getFilter().getRitual().contains("yes")) {
					specification = SpecificationUtil.getAndSpecification(specification,
							(root, query, cb) -> cb.equal(root.get("ritual"), true));
				}
				if(request.getFilter().getRitual().contains("no")) {
					specification = SpecificationUtil.getAndSpecification(specification,
							(root, query, cb) -> cb.equal(root.get("ritual"), false));
				}
			}
			if (request.getFilter().getConcentration()!=null && !request.getFilter().getConcentration().isEmpty()) {
				if(request.getFilter().getConcentration().contains("yes")) {
					specification = SpecificationUtil.getAndSpecification(specification,
							(root, query, cb) -> cb.equal(root.get("concentration"), true));
				}
				if(request.getFilter().getConcentration().contains("no")) {
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
			if (request.getFilter().getTimecast() !=null && !request.getFilter().getTimecast().isEmpty()) {
				for (String timecast : request.getFilter().getTimecast()) {
					String[] parts = timecast.split("\\s");
					int time = Integer.parseInt(parts[0]);
					TimeUnit unit = TimeUnit.valueOf(parts[1]);
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
						Join<TimeCast, Spell> join = root.join("times", JoinType.INNER);
						query.distinct(true);
						return cb.and(cb.equal(join.get("number"), time), cb.equal(join.get("unit"), unit));
					});
				}
			}
			if (request.getFilter().getDistance()!= null && !request.getFilter().getDistance().isEmpty()) {
				Specification<Spell> addSpec = null;
				for (String distance : request.getFilter().getDistance()) {
					addSpec = SpecificationUtil.getOrSpecification(addSpec,
							(root, query, cb) -> cb.like(root.get("distance"), "%" + distance + "%"));
				}
				specification = SpecificationUtil.getAndSpecification(specification, addSpec);
			}
			if (request.getFilter().getDuration() != null && !request.getFilter().getDuration().isEmpty()) {
				Specification<Spell> addSpec = null;
				for (String distance : request.getFilter().getDuration()) {
					addSpec = SpecificationUtil.getOrSpecification(addSpec,
							(root, query, cb) -> cb.like(root.get("duration"), "%" + distance + "%"));
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
		Collection<Spell> rules;
		if (pageable == null) {
			rules = spellRepository.findAll(specification, sort);
		} else {
			rules = spellRepository.findAll(specification, pageable).toList();
		}
		return rules
			.stream()
			.map(SpellApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Gets spell by english name")
	@ApiResponses(value = {
		  @ApiResponse(responseCode = "200", description = "Found the spell",
		    content = { @Content(mediaType = "application/json",
		      schema = @Schema(implementation = SpellDetailApi.class)) }),
		  @ApiResponse(responseCode = "400", description = "Invalid id supplied",
		    content = @Content),
		  @ApiResponse(responseCode = "404", description = "Spell not found",
		    content = @Content) })
	@PostMapping(value = "/api/v1/spells/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<SpellDetailApi> getSpell(@PathVariable String englishName) {
		Spell spell = spellRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
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

	@Operation(summary = "Gets filters for spells")
	@CrossOrigin
	@GetMapping(value = "/api/fvtt/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellsFvtt getSpells(String search, String exact){
		Specification<Spell> specification = null;
		if (search != null) {
			if (exact.isEmpty()) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), search.trim().toUpperCase());
			} else {
				String likeSearch = "%" + search + "%";
				specification = (root, query, cb) -> cb.or(cb.like(root.get("altName"), likeSearch),
					cb.like(root.get("englishName"), likeSearch),
					cb.like(root.get("name"), likeSearch));
			}
		}
		return new SpellsFvtt(spellRepository
			.findAll(specification)
			.stream()
			.map(SpellFvtt::new)
			.collect(Collectors.toList()));
	}

	@Operation(summary = "Gets spells filter", tags = "spells")
	@PostMapping("/api/v1/filters/spells")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = spellRepository.findBook(typeBook);
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
						.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
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
		List<FilterValueApi> values = Arrays.asList(new FilterValueApi("да", "yes"), new FilterValueApi("нет", "no"));
		ritualFilter.setValues(values);
		otherFilters.add(ritualFilter);

		FilterApi concentrationFilter = new FilterApi("Концентрация", "concentration");
		values = Arrays.asList(new FilterValueApi("требуется", "yes"), new FilterValueApi("не требуется", "no"));
		concentrationFilter.setValues(values);
		otherFilters.add(concentrationFilter);

		FilterApi damageTypeFilter = new FilterApi("Тип урона", "damageType");
		damageTypeFilter.setValues(
				DamageType.getSpellDamage().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(damageTypeFilter);

		FilterApi tagsFilter = new FilterApi("Тэги", "tag");
		tagsFilter.setValues(
			Arrays.stream(SpellTag.values())
				.map(value -> new FilterValueApi(value.getName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(tagsFilter);

		FilterApi timecastFilter = new FilterApi("Время накладывания", "timecast");
		timecastFilter.setValues(timecasts);
		otherFilters.add(timecastFilter);

		FilterApi distanceFilter = new FilterApi("Дистанция", "distance");
		distanceFilter.setValues(distancies);
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

	@PostMapping("/api/v1/filters/spells/{englishClassName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		if (heroClass.getEnglishName().equals("Warlock")) {
			otherFilters.add(getLevelsFilter(Spell.MAX_LEVEL));
		}
		else {
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

	@PostMapping("/api/v1/filters/spells/{englishClassName}/{englishArchetypeName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName, @PathVariable String englishArchetypeName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		if (heroClass.getEnglishName().equals("Warlock")) {
			otherFilters.add(getLevelsFilter(Spell.MAX_LEVEL));
		}
		else {
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
		}
		else {
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
				 .mapToObj(level -> new FilterValueApi(level == 0 ? "заговор" : String.valueOf(level),  String.valueOf(level)))
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
