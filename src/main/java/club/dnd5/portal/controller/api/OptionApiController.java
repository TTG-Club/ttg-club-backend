package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.OptionApi;
import club.dnd5.portal.dto.api.classes.OptionDetailApi;
import club.dnd5.portal.dto.api.classes.OptionRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.Option.OptionType;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.OptionRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
import javax.persistence.criteria.Order;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Class Option", description = "The Class Option API")
@RestController
public class OptionApiController {
	@Autowired
	private OptionRepository optionRepository;

	@Autowired
	private ClassRepository classRepository;

	@PostMapping(value = "/api/v1/options", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OptionApi> getOptions(@RequestBody OptionRequesApi request) {
		Specification<Option> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!CollectionUtils.isEmpty(request.getFilter().getBooks())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getClassOption())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<OptionType, Option> join = root.join("optionTypes", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getClassOption());
				});
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getLevels())) {
				if(request.getFilter().getLevels().contains("Нет")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.isNull(root.get("level")));
					request.getFilter().getLevels().remove("Нет");
				}
				if (!request.getFilter().getLevels().isEmpty()) {
					specification = SpecificationUtil.getAndSpecification(
							specification, (root, query, cb) -> root.get("level").in(request.getFilter().getLevels().stream().map(Integer::valueOf).collect(Collectors.toList())));
				}
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getPrerequsite())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("prerequisite").in(request.getFilter().getPrerequsite()));
			}
		}
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
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return optionRepository.findAll(specification, pageable).toList()
			.stream()
			.map(OptionApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/options/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OptionDetailApi> getOption(@PathVariable String englishName) {
		Option option = optionRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new OptionDetailApi(option));
	}

	@PostMapping("/api/v1/filters/options")
	public FilterApi getOptionFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = optionRepository.findBook(typeBook);
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

		FilterApi classOptionFilter = new FilterApi("Классовые особености", "classOption");
		classOptionFilter.setValues(
				Arrays.stream(Option.OptionType.values())
				 .map(ability -> new FilterValueApi(ability.getName(), ability.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(classOptionFilter);

		otherFilters.add(getLevelsFilter());
		otherFilters.add(getPrerequsitFilter(optionRepository.findAlldPrerequisite()));

		filters.setOther(otherFilters);
		return filters;
	}

	@PostMapping("/api/v1/filters/options/{englishClassName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getOptionType()));
		otherFilters.add(getPrerequsitFilter(optionRepository.findAlldPrerequisite(heroClass.getOptionType())));

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классовые особености");
		customFilter.setKey("classOption");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(heroClass.getOptionType().name());
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	@PostMapping("/api/v1/filters/options/{englishClassName}/{englishArchetypeName}")
	public FilterApi getByArchitypeFilter(@PathVariable String englishClassName, @PathVariable String englishArchetypeName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Archetype archetype = heroClass.getArchetypes().stream().filter(a -> a.getEnglishName().equalsIgnoreCase(englishArchetypeName.replace('_', ' '))).findFirst().get();

		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getOptionType()));
		otherFilters.add(getPrerequsitFilter(optionRepository.findAlldPrerequisite(heroClass.getOptionType())));

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классовые особености");
		customFilter.setKey("classOption");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(archetype.getOptionType().name());
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	private FilterApi getLevelsFilter() {
		FilterApi levelsFilter = new FilterApi("Требования к уровню", "levels");
		List<String> levels =  optionRepository.findAllLevels();
		levelsFilter.setValues(
				levels.stream()
				.map(v -> v == null ? "Нет" : v)
				.map(value -> new FilterValueApi(value, value))
				.collect(Collectors.toList()));
		return levelsFilter;
	}

	private FilterApi getLevelsFilter(OptionType optionType) {
		FilterApi levelsFilter = new FilterApi("Требования к уровню", "levels");
		List<String> levels =  optionRepository.findAllLevel(optionType);
		levelsFilter.setValues(
				levels.stream()
				.map(v -> v == null ? "Нет" : v)
				.map(value -> new FilterValueApi(value, value))
				.collect(Collectors.toList()));
		return levelsFilter;
	}

	private FilterApi getPrerequsitFilter(Collection<String> prerequisite) {
		FilterApi prerequisiteFilter = new FilterApi("Требования", "prerequsite");
		if (prerequisite.size() == 1) {
			prerequisiteFilter.setHidden(Boolean.TRUE);
		}
		prerequisiteFilter.setValues(
				prerequisite.stream()
				 .map(ability -> new FilterValueApi(ability, ability))
				 .collect(Collectors.toList()));
		return prerequisiteFilter;
	}
}
