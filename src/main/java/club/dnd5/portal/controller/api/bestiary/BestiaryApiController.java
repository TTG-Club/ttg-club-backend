package club.dnd5.portal.controller.api.bestiary;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastFilter;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
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
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.service.image.CreatureTokenService;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.TagBestiaryDatatableRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.service.bestiary.CreaturePredicateBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Tag(name = "Бестиарий", description = "API для сущест из бестиария")
@RestController
public class BestiaryApiController {
	private final BestiaryRepository beastRepository;
	private final TagBestiaryDatatableRepository tagRepository;
	private final ImageRepository imageRepository;
	private final TokenRepository tokenRepository;

	@Operation(summary = "Получение краткого списка существ")
	@PostMapping(value = "/api/v1/bestiary", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<BeastApi> getBestiary(@RequestBody BeastRequesApi request) {
		// Фильтры — в CreaturePredicateBuilder (QueryDSL, jsonb).
		Pageable pageable = PageAndSortUtil.getPageable(request, "id");
		return beastRepository.findAll(CreaturePredicateBuilder.build(request), pageable).toList()
			.stream()
			.map(BeastApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Получение существа по английскому имени")
	@PostMapping(value = "/api/v1/bestiary/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BeastDetailApi getBeast(@PathVariable String englishName) {
		Creature beast = beastRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		BeastDetailApi beastApi = new BeastDetailApi(beast);
		Collection<String> images = new ArrayList<>();
		tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(beast.getId(), CreatureTokenService.ROUND_TYPE)
			.ifPresent(token -> images.add(token.getUrl()));
		images.addAll(imageRepository.findAllByTypeAndRefId(ImageType.CREATURE, beast.getId()));
		if (!images.isEmpty()) {
			beastApi.setImages(images);
		}
		return beastApi;
	}

	@Operation(summary = "Фильтры для бестиария")
	@PostMapping("/api/v1/filters/bestiary")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = beastRepository.findBook(typeBook);
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

		FilterApi npcFilter = new FilterApi("Именованные НИП", "npc");
		npcFilter.setType("toggle");
		npcFilter.setValues(Collections.singletonList(
			new FilterValueApi("показать именованных НИП", "showNpc", Boolean.TRUE)));
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
				.map(value -> new FilterValueApi(value.getCyrillicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(typeFilter);

		FilterApi sizeFilter = new FilterApi("Размер существа", "size");
		sizeFilter.setValues(
			CreatureSize.getFilterSizes().stream()
				.map(value -> new FilterValueApi(value.getCyrillicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(sizeFilter);

		FilterApi tagFilter = new FilterApi("Тэги", "tag", false);
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
		values = DamageType.getVulnerability()
			.stream()
			.map(damage -> new FilterValueApi(damage.getShortName(), damage.name()))
			.collect(Collectors.toList());
		vulnerabilityDamageFilter.setValues(values);
		otherFilters.add(vulnerabilityDamageFilter);

		FilterApi resistDamageFilter = new FilterApi("Сопротивление к урону", "resistanceDamage");
		values = DamageType.getResistance()
			.stream()
			.map(damage -> new FilterValueApi(damage.getShortName(), damage.name()))
			.collect(Collectors.toList());
		resistDamageFilter.setValues(values);
		otherFilters.add(resistDamageFilter);

		FilterApi immunityDamageFilter = new FilterApi("Иммунитет к урону", "immunityDamage");
		values = DamageType.getImmunity()
			.stream()
			.map(damage -> new FilterValueApi(damage.getShortName(), damage.name()))
			.collect(Collectors.toList());
		immunityDamageFilter.setValues(values);
		otherFilters.add(immunityDamageFilter);

		FilterApi immunityConditionFilter = new FilterApi("Иммунитет к состояниям", "immunityCondition");
		values = Condition.getImmunity()
			.stream()
			.map(condition -> new FilterValueApi(condition.getCyrillicName(), condition.name()))
			.collect(Collectors.toList());
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
