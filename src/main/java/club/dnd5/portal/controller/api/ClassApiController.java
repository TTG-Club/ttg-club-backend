package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.classes.ClassApi;
import club.dnd5.portal.dto.api.classes.ClassDetailApi;
import club.dnd5.portal.dto.api.classes.ClassRequestApi;
import club.dnd5.portal.dto.api.classes.ClassSaveApi;
import club.dnd5.portal.dto.api.classes.ArchetypeEditApi;
import club.dnd5.portal.dto.api.classes.ArchetypeSaveApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeRepository;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag(name = "Класс", description = "API классов")
@RequiredArgsConstructor
@RestController
public class ClassApiController {
	private static final String ENTITY_TYPE_CLASS = "CLASS";
	private static final String ENTITY_TYPE_ARCHETYPE = "ARCHETYPE";

	private final ClassRepository classRepo;
	private final ArchetypeRepository archetypeRepository;
	private final ImageRepository imageRepository;
	private final BookRepository bookRepository;
	private final HeroClassTraitRepository heroClassTraitRepository;
	private final ArchetypeTraitRepository archetypeTraitRepository;
	private final AuditService auditService;

	@PostMapping(value = "/api/v1/classes", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ClassApi> getClasses(@RequestBody ClassRequestApi request) {
		Specification<HeroClass> specification = null;
		if (request.getFilter() != null) {
			if (!CollectionUtils.isEmpty(request.getFilter().getHitdice())) {
				specification = SpecificationUtil.getAndSpecification(null,
						(root, query, cb) -> root.get("diceHp").in(request.getFilter().getHitdice()));
			}
		}
		if (request.getSearch() != null && request.getSearch().getValue() != null && !request.getSearch().getValue().isEmpty()) {
			return classRepo.findAll(specification)
					.stream()
					.map(classObject -> new ClassApi(classObject, request))
					.filter(c -> !c.getArchetypes().isEmpty())
					.collect(Collectors.toList());
		}
		return classRepo.findAll(specification)
				.stream()
				.map(classObject -> new ClassApi(classObject, request))
				.filter(classApi -> request.getFilter() == null || request.getFilter().getBooks()
						.contains(classApi.getSource().getShortName()) || (classApi.isSidekick() && request.getFilter().getBooks().contains("TCE")))
				.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/classes/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> getClassInfo(@RequestBody(required = false) ClassRequestApi request, @PathVariable String englishName) {
		HeroClass heroClass = classRepo.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId());
		return ResponseEntity.ok(new ClassDetailApi(heroClass, images, request == null ? new ClassRequestApi() : request));
	}

	@PostMapping(value = "/api/v1/classes/{className}/{archetypeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> getArchetypeInfo(@RequestBody(required = false) ClassRequestApi request, @PathVariable String className,
			@PathVariable String archetypeName) {
		HeroClass heroClass = classRepo.findByEnglishName(className.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Archetype archetype = heroClass.getArchetypes().stream().filter(a -> a.getEnglishName().equalsIgnoreCase(archetypeName.replace('_', ' '))).findFirst().get();
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.SUBCLASS, archetype.getId());
		return ResponseEntity.ok(new ClassDetailApi(archetype, images, request == null ? new ClassRequestApi() : request));
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/classes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> createClass(@Valid @RequestBody ClassSaveApi request) {
		if (classRepo.findByEnglishName(request.getEnglishName()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class with the same englishName already exists");
		}
		HeroClass heroClass = new HeroClass();
		heroClass.setBook(getCustomBook());
		heroClass.setLevelDefenitions(Collections.emptyList());
		heroClass.setFeatureLevelDefenitions(Collections.emptyList());
		heroClass.setSpells(Collections.emptyList());
		heroClass.setTraits(Collections.emptyList());
		heroClass.setArchetypes(Collections.emptyList());
		applyClassRequest(heroClass, request);
		HeroClass saved = classRepo.saveAndFlush(heroClass);
		syncClassTraits(saved, request);
		saved.setTraits(heroClassTraitRepository.findAllByHeroClassIdAndArchitypeFalse(saved.getId()));
		auditService.record(ENTITY_TYPE_CLASS, saved.getId(), RevisionOperation.CREATE, request);
		return ResponseEntity.ok(new ClassDetailApi(saved, Collections.emptyList(), new ClassRequestApi()));
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/classes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> updateClass(@PathVariable Integer id, @Valid @RequestBody ClassSaveApi request) {
		HeroClass heroClass = classRepo.findById(id).orElseThrow(PageNotFoundException::new);
		classRepo.findByEnglishName(request.getEnglishName())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class with the same englishName already exists");
		});
		applyClassRequest(heroClass, request);
		HeroClass saved = classRepo.saveAndFlush(heroClass);
		syncClassTraits(saved, request);
		saved.setTraits(heroClassTraitRepository.findAllByHeroClassIdAndArchitypeFalse(saved.getId()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, saved.getId());
		auditService.record(ENTITY_TYPE_CLASS, saved.getId(), RevisionOperation.UPDATE, request);
		return ResponseEntity.ok(new ClassDetailApi(saved, images, new ClassRequestApi()));
	}

	@Operation(summary = "История изменений класса")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/classes/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getClassRevisions(@PathVariable Integer id) {
		classRepo.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE_CLASS, id);
	}

	@Operation(summary = "Состояние класса на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/classes/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ClassSaveApi getClassRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		classRepo.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE_CLASS, id, revision, ClassSaveApi.class);
	}

	@Operation(summary = "Восстановление класса из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/classes/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ClassDetailApi> restoreClassRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		ClassSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE_CLASS, id, revision, ClassSaveApi.class);
		return updateClass(id, snapshot);
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@PostMapping(value = "/api/v1/workshop/classes/{className}/{archetypeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArchetypeEditApi getArchetypeForEdit(@PathVariable String className, @PathVariable String archetypeName) {
		HeroClass heroClass = classRepo.findByEnglishName(className.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		Archetype archetype = archetypeRepository.findByHeroClassIdAndEnglishNameIgnoreCase(heroClass.getId(), archetypeName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		return new ArchetypeEditApi(archetype);
	}

	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/classes/archetypes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArchetypeEditApi updateArchetype(@PathVariable Integer id, @Valid @RequestBody ArchetypeSaveApi request) {
		Archetype archetype = archetypeRepository.findById(id).orElseThrow(PageNotFoundException::new);
		archetypeRepository.findByHeroClassIdAndEnglishNameIgnoreCase(archetype.getHeroClass().getId(), request.getEnglishName().trim())
			.filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archetype with the same englishName already exists");
			});
		archetype.setName(request.getName().trim()); archetype.setEnglishName(request.getEnglishName().trim());
		archetype.setGenitiveName(trimToNull(request.getGenitiveName())); archetype.setDescription(request.getDescription().trim());
		archetype.setLevel(request.getLevel()); archetype.setSpellcasterType(request.getSpellcasterType());
		archetype.setOptionType(request.getOptionType()); archetype.setPage(request.getPage());
		Archetype saved = archetypeRepository.saveAndFlush(archetype);
		syncArchetypeTraits(saved, request);
		saved.setFeats(archetypeTraitRepository.findAllByArchetypeId(saved.getId()));
		auditService.record(ENTITY_TYPE_ARCHETYPE, saved.getId(), RevisionOperation.UPDATE, request);
		return new ArchetypeEditApi(saved);
	}

	@Operation(summary = "История изменений архетипа")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/classes/archetypes/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getArchetypeRevisions(@PathVariable Integer id) {
		archetypeRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE_ARCHETYPE, id);
	}

	@Operation(summary = "Состояние архетипа на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/classes/archetypes/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArchetypeSaveApi getArchetypeRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		archetypeRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE_ARCHETYPE, id, revision, ArchetypeSaveApi.class);
	}

	@Operation(summary = "Восстановление архетипа из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/classes/archetypes/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArchetypeEditApi restoreArchetypeRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		ArchetypeSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE_ARCHETYPE, id, revision, ArchetypeSaveApi.class);
		return updateArchetype(id, snapshot);
	}

	private void syncArchetypeTraits(Archetype archetype, ArchetypeSaveApi request) {
		if (request.getTraits() == null) return;
		Map<Integer, ArchetypeTrait> existing = archetypeTraitRepository.findAllByArchetypeId(archetype.getId()).stream()
			.collect(Collectors.toMap(ArchetypeTrait::getId, trait -> trait));
		List<ArchetypeTrait> traits = request.getTraits().stream()
			.filter(trait -> StringUtils.hasText(trait.getName()) && StringUtils.hasText(trait.getDescription()))
			.map(value -> {
				ArchetypeTrait trait = value.getId() == null ? new ArchetypeTrait() : existing.getOrDefault(value.getId(), new ArchetypeTrait());
				trait.setName(value.getName().trim()); trait.setSuffix(trimToNull(value.getSuffix())); trait.setLevel(value.getLevel());
				trait.setDescription(value.getDescription().trim()); trait.setChild(trimToNull(value.getChild()));
				trait.setArchetype(archetype); trait.setBook(archetype.getBook()); return trait;
			}).collect(Collectors.toList());
		archetypeTraitRepository.saveAll(traits); archetypeTraitRepository.flush();
		List<Integer> retained = traits.stream().map(ArchetypeTrait::getId).collect(Collectors.toList());
		existing.values().stream().filter(trait -> !retained.contains(trait.getId())).forEach(archetypeTraitRepository::delete);
	}

	@PostMapping("/api/v1/filters/classes")
	public FilterApi getClassFilter() {
		return getClassFilters();
	}
	private FilterApi getClassFilters() {
		FilterApi filters = new FilterApi();
		List<FilterApi> classSources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = Stream.concat(classRepo.findBook(typeBook).stream(), archetypeRepository.findBook(typeBook).stream()).distinct().collect(Collectors.toList());
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
						.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
						.collect(Collectors.toList()));
				classSources.add(filter);
			}
		}
		filters.setSources(classSources);

		List<FilterApi> others = new ArrayList<>();
		FilterApi hillDiceFilter = new FilterApi("Кость хитов", "hitdice");
		hillDiceFilter.setValues(
				Stream.of(Dice.d6, Dice.d8, Dice.d10, Dice.d12)
				.map(dice -> new FilterValueApi(dice.getName(), dice.getMaxValue()))
				.collect(Collectors.toList())
		);
		others.add(hillDiceFilter);
		filters.setOther(others);
		return filters;
	}

	private void applyClassRequest(HeroClass heroClass, ClassSaveApi request) {
		heroClass.setName(request.getName().trim());
		heroClass.setEnglishName(request.getEnglishName().trim());
		heroClass.setAccusativeName(trimToNull(request.getAccusativeName()));
		heroClass.setDescription(request.getDescription().trim());
		heroClass.setDiceHp(request.getDiceHp());
		heroClass.setArmor(trimToNull(request.getArmor()));
		heroClass.setWeapon(trimToNull(request.getWeapon()));
		heroClass.setTools(trimToNull(request.getTools()));
		heroClass.setSavingThrows(trimToNull(request.getSavingThrows()));
		heroClass.setArchetypeName(trimToNull(request.getArchetypeName()));
		heroClass.setEquipment(trimToNull(request.getEquipment()));
		heroClass.setSpellAbility(request.getSpellAbility());
		heroClass.setSpellcasterType(request.getSpellcasterType());
		heroClass.setPrimaryAbilities(request.getPrimaryAbilities() == null ? new ArrayList<>() : new ArrayList<>(request.getPrimaryAbilities()));
		heroClass.setEnabledArhitypeLevel(request.getEnabledArhitypeLevel());
		heroClass.setSkillAvailableCount(request.getSkillAvailableCount());
		heroClass.setAvailableSkills(request.getAvailableSkills() == null ? new ArrayList<>() : new ArrayList<>(request.getAvailableSkills()));
		heroClass.setOptionType(request.getOptionType());
		heroClass.setSlotsReset(request.getSlotsReset());
		heroClass.setSidekick(request.isSidekick());
		heroClass.setIcon(trimToNull(request.getIcon()));
		heroClass.setPage(request.getPage());
	}

	private void syncClassTraits(HeroClass heroClass, ClassSaveApi request) {
		if (request.getClassTraits() == null) {
			return;
		}
		List<HeroClassTrait> existingTraits = heroClassTraitRepository.findAllByHeroClassIdAndArchitypeFalse(heroClass.getId());
		Map<Integer, HeroClassTrait> existingById = existingTraits.stream()
			.collect(Collectors.toMap(HeroClassTrait::getId, trait -> trait));
		List<HeroClassTrait> traits = request.getClassTraits().stream()
			.filter(traitRequest -> StringUtils.hasText(traitRequest.getName()))
			.filter(traitRequest -> StringUtils.hasText(traitRequest.getDescription()))
			.map(traitRequest -> {
				HeroClassTrait trait = traitRequest.getId() == null
					? new HeroClassTrait()
					: existingById.getOrDefault(traitRequest.getId(), new HeroClassTrait());
				trait.setName(traitRequest.getName().trim());
				trait.setSuffix(trimToNull(traitRequest.getSuffix()));
				trait.setLevel(traitRequest.getLevel());
				trait.setDescription(traitRequest.getDescription().trim());
				trait.setOptional(traitRequest.isOptional() ? 1 : 0);
				trait.setChild(trimToNull(traitRequest.getChild()));
				trait.setArchitype(false);
				trait.setHeroClass(heroClass);
				trait.setBook(heroClass.getBook());
				return trait;
			})
			.collect(Collectors.toList());
		heroClassTraitRepository.saveAll(traits);
		heroClassTraitRepository.flush();
		List<Integer> ids = traits.stream()
			.map(HeroClassTrait::getId)
			.collect(Collectors.toList());
		if (ids.isEmpty()) {
			heroClassTraitRepository.deleteClassTraits(heroClass.getId());
		} else {
			heroClassTraitRepository.deleteClassTraitsNotIn(heroClass.getId(), ids);
		}
	}

	private Book getCustomBook() {
		return bookRepository.findFirstByType(TypeBook.CUSTOM)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CUSTOM source book is not configured"));
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
