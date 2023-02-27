package club.dnd5.portal.controller.api.bestiary;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastFilter;
import club.dnd5.portal.dto.api.bestiary.BeastlRequesApi;
import club.dnd5.portal.dto.fvtt.export.FBeastiary;
import club.dnd5.portal.dto.fvtt.export.FCreature;
import club.dnd5.portal.dto.fvtt.plutonium.FBeast;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.creature.*;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.BestiaryDatatableRepository;
import club.dnd5.portal.repository.datatable.TagBestiaryDatatableRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Tag(name = "Bestiary", description = "The Bestiary API")
@RestController
public class BestiaryApiController {
	@Autowired
	private BestiaryDatatableRepository beastRepository;

	@Autowired
	private TagBestiaryDatatableRepository tagRepository;

	@Autowired
	private ImageRepository imageRepository;

	@PostMapping(value = "/api/v1/bestiary", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<BeastApi> getBestiary(@RequestBody BeastlRequesApi request) {
		Specification<Creature> specification = null;

		DataTablesInput input = new DataTablesInput();
		List<Column> columns = new ArrayList<>(3);
		Column column = new Column();
		column.setData("name");
		column.setName("name");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		column.setSearch(new Search("", Boolean.FALSE));
		columns.add(column);

		column = new Column();
		column.setData("englishName");
		column.setName("englishName");
		column.setSearch(new Search("", Boolean.FALSE));
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		columns.add(column);

		column = new Column();
		column.setData("altName");
		column.setName("altName");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.FALSE);

		columns.add(column);
		if (request.getOrders()!=null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(
							order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField()))
						)
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}
		input.setColumns(columns);
		input.setLength(request.getLimit() != null ? request.getLimit() : -1);
		if (request.getPage() != null && request.getLimit()!=null) {
			input.setStart(request.getPage() * request.getLimit());
		}
		if (request.getSearch() != null) {
			if (request.getSearch().getValue() != null && !request.getSearch().getValue().isEmpty()) {
				if (request.getSearch().getExact() != null && request.getSearch().getExact()) {
					specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
				} else {
					input.getSearch().setValue(request.getSearch().getValue());
					input.getSearch().setRegex(Boolean.FALSE);
				}
			}
		}
		Optional<BeastFilter> filter = Optional.ofNullable(request.getFilter());
		if (filter.isPresent() && filter.map(BeastFilter::getNpc).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.notEqual(root.get("raceId"), 102));
		}
		if (!filter.map(BeastFilter::getChallengeRatings).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("challengeRating").in(request.getFilter().getChallengeRatings()));
		}
		if (!filter.map(BeastFilter::getTypes).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(
				specification, (root, query, cb) -> root.get("type").in(request.getFilter().getTypes()));
		}
		if (!filter.map(BeastFilter::getSizes).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(
				specification, (root, query, cb) -> root.get("size").in(request.getFilter().getSizes()));
		}
		if (!filter.map(BeastFilter::getTags).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("races", JoinType.INNER);
				query.distinct(true);
				return cb.and(join.get("id").in(request.getFilter().getTags()));
			});
		}
		if (!filter.map(BeastFilter::getBooks).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Book, Spell> join = root.join("book", JoinType.INNER);
				return join.get("source").in(request.getFilter().getBooks());
			});
		}
		if (!filter.map(BeastFilter::getMoving).orElseGet(Collections::emptyList).isEmpty()) {
			if (request.getFilter().getMoving().contains("fly")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("flySpeed")));
			}
			if (request.getFilter().getMoving().contains("hover")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("hover")));
			}
			if (request.getFilter().getMoving().contains("climbs")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("climbingSpeed")));
			}
			if (request.getFilter().getMoving().contains("swim")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("swimmingSpped")));
			}
			if (request.getFilter().getMoving().contains("digger")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("diggingSpeed")));
			}
		}
		if (!filter.map(BeastFilter::getEnvironments).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("habitates", JoinType.INNER);
				query.distinct(true);
				return join.in(request.getFilter().getEnvironments());
			});
		}
		if (!filter.map(BeastFilter::getVulnerabilityDamage).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("vulnerabilityDamages", JoinType.INNER);
				query.distinct(true);
				return join.in(request.getFilter().getVulnerabilityDamage());
			});
		}
		if (!filter.map(BeastFilter::getResistanceDamage).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("resistanceDamages", JoinType.INNER);
				query.distinct(true);
				return join.in(request.getFilter().getResistanceDamage());
			});
		}
		if (!filter.map(BeastFilter::getImmunityDamage).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("immunityDamages", JoinType.INNER);
				query.distinct(true);
				return join.in(request.getFilter().getImmunityDamage());
			});
		}
		if (!filter.map(BeastFilter::getImmunityCondition).orElseGet(Collections::emptyList).isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Object, Object> join = root.join("immunityStates", JoinType.INNER);
				query.distinct(true);
				return join.in(request.getFilter().getImmunityCondition());
			});
		}
		if (!filter.map(BeastFilter::getSenses).orElseGet(Collections::emptyList).isEmpty()) {
			if (request.getFilter().getSenses().contains("darkvision")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("darkvision")));
			}
			if (request.getFilter().getSenses().contains("trysight")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("trysight")));
			}
			if (request.getFilter().getSenses().contains("blindsight")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("blindsight")));
			}
			if (request.getFilter().getSenses().contains("tremmor")) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNotNull(root.get("vibration")));
			}
		}
		if (!filter.map(BeastFilter::getFeatures).orElseGet(Collections::emptyList).isEmpty()) {
			Specification<Creature> addSpec = null;
			if (request.getFilter().getFeatures().contains("lair")) {
				addSpec = SpecificationUtil.getOrSpecification(null, (root, query, cb) ->
					cb.isNotNull(root.get("lair"))
				);
				request.getFilter().getFeatures().remove("lair");
			}
			if (request.getFilter().getFeatures().contains("legendary")) {
				addSpec = SpecificationUtil.getOrSpecification(addSpec, (root, query, cb) -> {
					Join<Action, Creature> join = root.join("actions", JoinType.INNER);
					query.distinct(true);
					return cb.equal(join.get("actionType"), ActionType.LEGENDARY);
				});
				request.getFilter().getFeatures().remove("legendary");
			}
			for (String featureName : request.getFilter().getFeatures()) {
				addSpec = SpecificationUtil.getOrSpecification(addSpec, (root, query, cb) -> {
					Join<CreatureFeat, Creature> join = root.join("feats", JoinType.INNER);
					query.distinct(true);
					return cb.like(join.get("name"), "%" + featureName + "%");
				});
			}
			specification = SpecificationUtil.getAndSpecification(specification, addSpec);
		}
		return beastRepository.findAll(input, specification, specification, BeastApi::new).getData();
	}

	@PostMapping(value = "/api/v1/bestiary/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BeastDetailApi getBeast(@PathVariable String englishName) {
		Creature beast = beastRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		BeastDetailApi beastApi = new BeastDetailApi(beast);
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CREATURE, beast.getId());
		if (!images.isEmpty()) {
			beastApi.setImages(images);
		}
		return beastApi;
	}

	@GetMapping("/api/fvtt/v1/bestiary/{id}")
	public ResponseEntity<FCreature> getCreature(HttpServletResponse response, @PathVariable Integer id){
		Creature creature = beastRepository.findById(id).orElseThrow(PageNotFoundException::new);
		response.setContentType("application/json");
		String file = String.format("attachment; filename=\"%s.json\"", creature.getEnglishName());
		response.setHeader("Content-Disposition", file);
		return ResponseEntity.ok(new FCreature(creature));
	}

	@CrossOrigin
	@GetMapping("/api/fvtt/v1/bestiary")
	public FBeastiary getCreatures(){
		List<FBeast> list = ((Collection<Creature>) beastRepository.findAll())
				.stream()
				.map(FBeast::new)
				.collect(Collectors.toList());
		return new FBeastiary(list);
	}

	@PostMapping("/api/v1/filters/bestiary")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = beastRepository.findBook(typeBook);
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

		FilterApi npcFilter = new FilterApi("Именнованые НИП", "npc");
		npcFilter.setType("toggle");
		npcFilter.setValues(Collections.singletonList(new FilterValueApi("показать именованных НИП", "showNpc", Boolean.TRUE)));
		otherFilters.add(npcFilter);

		FilterApi crFilter = new FilterApi("Уровень опасности", "challengeRating");
		crFilter.setExpand(Boolean.TRUE);
		List<FilterValueApi> values = new ArrayList<>();
		values.add(new FilterValueApi("не определен", "—"));
		values.add(new FilterValueApi("0", "0"));
		values.add(new FilterValueApi("1/8", "1/8"));
		values.add(new FilterValueApi("1/4", "1/4"));
		values.add(new FilterValueApi("1/2", "1/2"));
		values.addAll(
				IntStream.rangeClosed(1, 30)
				.mapToObj(value -> new FilterValueApi(String.valueOf(value), value))
				.collect(Collectors.toList()));
		crFilter.setValues(values);
		otherFilters.add(crFilter);

		FilterApi typeFilter = new FilterApi("Тип существа", "type");
		typeFilter.setValues(
				CreatureType.getFilterTypes().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(typeFilter);

		FilterApi sizeFilter = new FilterApi("Размер существа", "size");
		sizeFilter.setValues(
				CreatureSize.getFilterSizes().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(sizeFilter);

		FilterApi tagFilter = new FilterApi("Тэги", "tag");
		tagFilter.setValues(
				tagRepository.findByOrderByName().stream()
				 .map(value -> new FilterValueApi(value.getName(), value.getId()))
				 .collect(Collectors.toList()));
		otherFilters.add(tagFilter);

		FilterApi moveFilter = new FilterApi("Перемещение", "moving");
		values = new ArrayList<>(3);
		values.add(new FilterValueApi("летает", "fly"));
		values.add(new FilterValueApi("парит", "hover"));
		values.add(new FilterValueApi("лазает", "climbs"));
		values.add(new FilterValueApi("плавает", "swim"));
		values.add(new FilterValueApi("копает", "digger"));
		moveFilter.setValues(values);
		otherFilters.add(moveFilter);

		FilterApi sanseFilter = new FilterApi("Чувства", "senses");
		values = new ArrayList<>(3);
		values.add(new FilterValueApi("тёмное зрение", "darkvision"));
		values.add(new FilterValueApi("истинное зрение", "trysight"));
		values.add(new FilterValueApi("слепое зрение", "blindsight"));
		values.add(new FilterValueApi("чувство вибрации", "tremmor"));
		sanseFilter.setValues(values);
		otherFilters.add(sanseFilter);

		FilterApi vulnerabilityDamageFilter = new FilterApi("Уязвимость к урону", "vulnerabilityDamage");
		values = DamageType.getVulnerability().stream().map(damage-> new FilterValueApi(damage.getCyrilicName(), damage.name())).collect(Collectors.toList());
		vulnerabilityDamageFilter.setValues(values);
		otherFilters.add(vulnerabilityDamageFilter);

		FilterApi resistDamageFilter = new FilterApi("Сопротивление к урону", "resistanceDamage");
		values = DamageType.getResistance().stream().map(damage-> new FilterValueApi(damage.getCyrilicName(), damage.name())).collect(Collectors.toList());
		resistDamageFilter.setValues(values);
		otherFilters.add(resistDamageFilter);

		FilterApi immunityDamageFilter = new FilterApi("Иммунитет к урону", "immunityDamage");
		values = DamageType.getImmunity().stream().map(damage-> new FilterValueApi(damage.getCyrilicName(), damage.name())).collect(Collectors.toList());
		immunityDamageFilter.setValues(values);
		otherFilters.add(immunityDamageFilter);

		FilterApi immunityConditionFilter = new FilterApi("Иммунитет к состояниям", "immunityCondition");
		values = Condition.getImmunity().stream().map(condition-> new FilterValueApi(condition.getCyrilicName(), condition.name())).collect(Collectors.toList());
		immunityConditionFilter.setValues(values);
		otherFilters.add(immunityConditionFilter);

		FilterApi featureFilter = new FilterApi("Умения", "features");
		values = new ArrayList<>(3);
		values.add(new FilterValueApi("сопротивление магии", "Сопротивление магии"));
		values.add(new FilterValueApi("врождённое колдовство", "Врождённое колдовство"));
		values.add(new FilterValueApi("использование заклинаний", "Использование заклинаний"));
		values.add(new FilterValueApi("легендарное сопротивление", "Легендарное сопротивление"));
		values.add(new FilterValueApi("легендарные действия", "legendary"));
		values.add(new FilterValueApi("логово", "lair"));
		featureFilter.setValues(values);
		otherFilters.add(featureFilter);

		FilterApi environmentFilter = new FilterApi("Места обитания", "environment");
		environmentFilter.setValues(
				HabitatType.allTypes().stream()
				 .map(value -> new FilterValueApi(value.getName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(environmentFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
