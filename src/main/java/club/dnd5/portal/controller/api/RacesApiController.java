package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.RaceFilter;
import club.dnd5.portal.dto.api.classes.RaceRequestApi;
import club.dnd5.portal.dto.api.races.RaceApi;
import club.dnd5.portal.dto.api.races.RaceDetailApi;
import club.dnd5.portal.dto.api.races.RaceSaveApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityBonus;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.RaceAbilityBonusRepository;
import club.dnd5.portal.repository.datatable.RaceFeatureRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Расы и происхождения", description = "API по рассам и происхождениям")
@RestController
public class RacesApiController {
	private static final String ENTITY_TYPE = "RACE";

	private final RaceRepository raceRepository;
	private final ImageRepository imageRepository;
	private final BookResolver bookResolver;
	private final RaceAbilityBonusRepository raceAbilityBonusRepository;
	private final RaceFeatureRepository raceFeatureRepository;
	private final AuditService auditService;

	@PostMapping(value = "/api/v1/races", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RaceApi> getRaces(@RequestBody RaceRequestApi request) {
		Specification<Race> specification;

		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		} else {
			specification = Specification.where((root, query, cb) -> cb.isNull(root.get("parent")));
		}

		if (request.getFilter() != null && request.getFilter().getBooks() != null && !request.getFilter().getBooks().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Book, Race> join = root.join("book", JoinType.INNER);
				return join.get("source").in(request.getFilter().getBooks());
			});
		}

		if (request.getFilter() != null) {
			for (String ability : request.getFilter().getAbilities()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<AbilityType, Race> join = root.join("bonuses", JoinType.LEFT);
					query.distinct(true);
					return cb.equal(join.get("ability"), AbilityType.valueOf(ability));
				});
			}
			if (request.getFilter().getSkills().contains("darkvision")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("darkvision")));
			}
			if (request.getFilter().getSkills().contains("fly")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("fly")));
			}
			if (request.getFilter().getSkills().contains("swim")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("swim")));
			}
			if (request.getFilter().getSkills().contains("climb")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("climb")));
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return raceRepository.findAll(specification, pageable)
			.stream()
			.map(race -> new RaceApi(race, Optional.of(request)
				.map(RaceRequestApi::getFilter)
				.map(RaceFilter::getBooks)
				.orElse(Collections.emptySet()))
			)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/races/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RaceDetailApi> getRace(
		@PathVariable String englishName,
		@RequestBody RaceRequestApi request) {
		Race race = raceRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		RaceDetailApi raceApi = new RaceDetailApi(race, Optional.of(request)
			.map(RaceRequestApi::getFilter)
			.map(RaceFilter::getBooks)
			.orElse(Collections.emptySet()
		));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId());
		if (!images.isEmpty()) {
			raceApi.setImages(images);
		}
		return ResponseEntity.ok(raceApi);
	}

	@PostMapping(value = "/api/v1/races/{englishRaceName}/{englishSubraceName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RaceDetailApi getSubrace(
		@PathVariable String englishRaceName,
		@PathVariable String englishSubraceName,
		@RequestBody RaceRequestApi request) {
		Optional<Race> race = raceRepository.findBySubrace(englishRaceName.replace('_', ' '), englishSubraceName.replace('_', ' '));
		RaceDetailApi raceApi = new RaceDetailApi(race.get(), Optional.of(request).map(RaceRequestApi::getFilter).map(RaceFilter::getBooks).orElse(Collections.emptySet()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.get().getId());
		if (!images.isEmpty()) {
			raceApi.setImages(images);
		}
		return raceApi;
	}

	@PostMapping("/api/v1/filters/races")
	public FilterApi getRacesFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = raceRepository.findBook(typeBook);
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
		FilterApi levelFilter = new FilterApi("Увеличение характеристик", "abilities");
		levelFilter.setValues(
				EnumSet.of(AbilityType.STRENGTH, AbilityType.DEXTERITY,AbilityType.CONSTITUTION,AbilityType.INTELLIGENCE,AbilityType.WISDOM,AbilityType.CHARISMA)
				.stream()
				.map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(levelFilter);

		FilterApi skillFilter = new FilterApi("Способности", "skills");
		List<FilterValueApi> values = new ArrayList<>();
		values.add(new FilterValueApi("тёмное зрение", "darkvision"));
		values.add(new FilterValueApi("полет", "fly"));
		values.add(new FilterValueApi("плавание", "swim"));
		values.add(new FilterValueApi("лазание", "climb"));

		skillFilter.setValues(values);
		otherFilters.add(skillFilter);
		filters.setOther(otherFilters);
		return filters;
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/races", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RaceDetailApi> createRace(@Valid @RequestBody RaceSaveApi request) {
		if (raceRepository.findByEnglishName(request.getEnglishName()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Race with the same englishName already exists");
		}
		Race race = new Race();
		race.setBook(bookResolver.getCustomBook());
		race.setSubRaces(Collections.emptyList());
		race.setNames(Collections.emptyList());
		race.setNicknames(Collections.emptyList());
		applyRaceRequest(race, request);
		Race saved = raceRepository.saveAndFlush(race);
		syncAbilities(saved, request);
		syncFeatures(saved, request);
		saved.setBonuses(raceAbilityBonusRepository.findAllByRaceId(saved.getId()));
		saved.setFeatures(raceFeatureRepository.findAllByRaceId(saved.getId()));
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.CREATE, request);
		return ResponseEntity.ok(new RaceDetailApi(saved, Collections.emptySet()));
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/races/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RaceDetailApi> updateRace(@PathVariable Integer id, @Valid @RequestBody RaceSaveApi request) {
		Race race = raceRepository.findById(id).orElseThrow(PageNotFoundException::new);
		raceRepository.findByEnglishName(request.getEnglishName())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Race with the same englishName already exists");
			});
		applyRaceRequest(race, request);
		Race saved = raceRepository.saveAndFlush(race);
		syncAbilities(saved, request);
		syncFeatures(saved, request);
		saved.setBonuses(raceAbilityBonusRepository.findAllByRaceId(saved.getId()));
		saved.setFeatures(raceFeatureRepository.findAllByRaceId(saved.getId()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, saved.getId());
		RaceDetailApi raceApi = new RaceDetailApi(saved, Collections.emptySet());
		if (!images.isEmpty()) {
			raceApi.setImages(images);
		}
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.UPDATE, request);
		return ResponseEntity.ok(raceApi);
	}

	@Operation(summary = "История изменений расы")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/races/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getRaceRevisions(@PathVariable Integer id) {
		raceRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние расы на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/races/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RaceSaveApi getRaceRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		raceRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, RaceSaveApi.class);
	}

	@Operation(summary = "Восстановление расы из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/races/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RaceDetailApi> restoreRaceRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		RaceSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, RaceSaveApi.class);
		return updateRace(id, snapshot);
	}

	private void applyRaceRequest(Race race, RaceSaveApi request) {
		race.setName(request.getName().trim());
		race.setAltName(trimToNull(request.getAltName()));
		race.setEnglishName(request.getEnglishName().trim());
		race.setMinAge(request.getMinAge());
		race.setMaxAge(request.getMaxAge());
		race.setDescription(request.getDescription().trim());
		race.setParent(request.getParentId() == null ? null : raceRepository.findById(request.getParentId()).orElseThrow(PageNotFoundException::new));
		race.setSize(request.getSize());
		race.setType(request.getType());
		race.setDarkvision(request.getDarkvision());
		race.setSpeed(request.getSpeed());
		race.setFly(request.getFly());
		race.setClimb(request.getClimb());
		race.setSwim(request.getSwim());
		race.setOrigin(request.getOrigin());
		race.setView(request.isView());
		race.setIcon(trimToNull(request.getIcon()));
		race.setPage(request.getPage());
		bookResolver.find(request.getSource()).ifPresent(race::setBook);
	}

	private void syncAbilities(Race race, RaceSaveApi request) {
		if (request.getAbilities() == null) {
			return;
		}
		Map<Integer, AbilityBonus> existingById = raceAbilityBonusRepository.findAllByRaceId(race.getId())
			.stream()
			.collect(Collectors.toMap(AbilityBonus::getId, ability -> ability));
		List<AbilityBonus> abilities = request.getAbilities().stream()
			.filter(abilityRequest -> abilityRequest.getAbility() != null)
			.map(abilityRequest -> {
				AbilityBonus ability = abilityRequest.getId() == null
					? new AbilityBonus()
					: existingById.getOrDefault(abilityRequest.getId(), new AbilityBonus());
				ability.setRaceId(race.getId());
				ability.setAbility(abilityRequest.getAbility());
				ability.setBonus(abilityRequest.getBonus());
				return ability;
			})
			.collect(Collectors.toList());
		raceAbilityBonusRepository.saveAll(abilities);
		raceAbilityBonusRepository.flush();
		race.setBonuses(abilities);
		raceRepository.saveAndFlush(race);
		List<Integer> ids = abilities.stream().map(AbilityBonus::getId).collect(Collectors.toList());
		if (ids.isEmpty()) {
			raceAbilityBonusRepository.deleteByRaceId(race.getId());
		} else {
			raceAbilityBonusRepository.deleteByRaceIdAndIdNotIn(race.getId(), ids);
		}
	}

	private void syncFeatures(Race race, RaceSaveApi request) {
		if (request.getFeatures() == null) {
			return;
		}
		Map<Integer, Feature> existingById = raceFeatureRepository.findAllByRaceId(race.getId())
			.stream()
			.collect(Collectors.toMap(Feature::getId, feature -> feature));
		List<Feature> features = request.getFeatures().stream()
			.filter(featureRequest -> StringUtils.hasText(featureRequest.getName()))
			.filter(featureRequest -> StringUtils.hasText(featureRequest.getDescription()))
			.map(featureRequest -> {
				Feature feature = featureRequest.getId() == null
					? new Feature()
					: existingById.getOrDefault(featureRequest.getId(), new Feature());
				feature.setRaceId(race.getId());
				feature.setName(featureRequest.getName().trim());
				feature.setEnglishName(trimToNull(featureRequest.getEnglishName()));
				feature.setDescription(featureRequest.getDescription().trim());
				feature.setFeature(featureRequest.isFeature());
				feature.setReplaceFeatureId(featureRequest.getReplaceFeatureId());
				return feature;
			})
			.collect(Collectors.toList());
		raceFeatureRepository.saveAll(features);
		raceFeatureRepository.flush();
		race.setFeatures(features);
		raceRepository.saveAndFlush(race);
		List<Integer> ids = features.stream().map(Feature::getId).collect(Collectors.toList());
		if (ids.isEmpty()) {
			raceFeatureRepository.deleteByRaceId(race.getId());
		} else {
			raceFeatureRepository.deleteByRaceIdAndIdNotIn(race.getId(), ids);
		}
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
