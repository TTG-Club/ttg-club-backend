package club.dnd5.portal.controller;

import club.dnd5.portal.dto.classes.ClassFetureDto;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.repository.datatable.OptionRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Hidden
@Controller
public class ClassController {
	private static final String BASE_URL = "https://ttg.club/classes";

	private static final String[] prerequisites = { "Нет", " 5", " 6", " 7", " 9", "11", "12", "15", "17", "18" };

	@Autowired
	private ClassRepository classRepository;
	@Autowired
	private HeroClassTraitRepository traitRepository;
	@Autowired
	private ArchetypeTraitRepository archetypeTraitRepository;
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private OptionRepository optionRepository;

	@GetMapping("/classes")
	public String getClasses(Model model) {
		model.addAttribute("metaTitle", "Классы (Classes) D&D 5e");
		model.addAttribute("menuTitle", "Классы");
		model.addAttribute("metaUrl", BASE_URL);
		return "spa";
	}

	@GetMapping("/classes/{name}")
	public String getClass(Model model, @PathVariable String name) {
		HeroClass heroClass = classRepository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Классы D&D 5e", heroClass.getCapitalazeName(), heroClass.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, heroClass.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - описание класса персонажа по D&D 5-редакции", heroClass.getCapitalazeName(), heroClass.getEnglishName()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Классы");
		return "spa";
	}

	@GetMapping("/classes/{name}/{archetype}")
	public String getArchetype(Model model, @PathVariable String name, @PathVariable String archetype, HttpServletRequest request) {
		String englishName = name.replace("_", " ");
		HeroClass heroClass = classRepository.findByEnglishName(englishName).orElseThrow(PageNotFoundException::new);
		Archetype selectedArchetype = heroClass.getArchetypes().stream()
				.filter(a -> a.getEnglishName().equalsIgnoreCase(archetype.replace('_', ' ')))
				.findFirst().orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s - %s (%s) | Классы | Подклассы D&D 5e",
				StringUtils.capitalize(selectedArchetype.getName().toLowerCase()), heroClass.getCapitalazeName(), heroClass.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s/%s", BASE_URL, heroClass.getUrlName(), selectedArchetype.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s - описание %s класса %s из D&D 5 редакции",
				selectedArchetype.getName(), heroClass.getArchetypeName(), heroClass.getCapitalazeName()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.SUBCLASS, selectedArchetype.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Классы");
		return "spa";
	}

	@GetMapping("/classes/fragment/{englishName}")
	public String getFragmentClasses(Model model, @PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		List<ClassFetureDto> features = new ArrayList<>();
		heroClass.getTraits().stream()
			.filter(f -> !f.isArchitype())
			.map(f -> new ClassFetureDto(f, heroClass.getName()))
			.forEach(f -> features.add(f));
		Map<Integer, Set<ClassFetureDto>> archetypeTraits = heroClass.getArchetypes()
				.stream().flatMap(a -> a.getFeats().stream())
				.collect(
						Collectors.groupingBy(
								f -> f.getArchetype().getId(),
								Collectors.mapping(f -> new ClassFetureDto(
										f, f.getArchetype().getGenitiveName()),
										Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ClassFetureDto::getLevel).thenComparing(ClassFetureDto::getName))))
								)
				);
		Collections.sort(features, Comparator.comparing(ClassFetureDto::getLevel));
		model.addAttribute("features", features);
		model.addAttribute("heroClass", heroClass);
		model.addAttribute("archetypeTraits", archetypeTraits);
		model.addAttribute("order", "[[ 1, 'asc' ]]");
		model.addAttribute("selectedArchetypeName", heroClass.getArchetypeName());
		return "fragments/class :: view";
	}

	@GetMapping("/classes/fragment_id/{id}")
	public String getFragmentClassesById(Model model, @PathVariable Integer id) {
		HeroClass heroClass = classRepository.findById(id).orElseThrow(IllegalArgumentException::new);
		List<ClassFetureDto> features = new ArrayList<>();
		heroClass.getTraits().stream()
			.filter(f -> !f.isArchitype())
			.map(f -> new ClassFetureDto(f, heroClass.getName()))
			.forEach(f -> features.add(f));
		Map<Integer, Set<ClassFetureDto>> archetypeTraits = heroClass.getArchetypes()
				.stream().flatMap(a -> a.getFeats().stream())
				.collect(
						Collectors.groupingBy(
								f -> f.getArchetype().getId(),
								Collectors.mapping(f -> new ClassFetureDto(
										f, f.getArchetype().getGenitiveName()),
										Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ClassFetureDto::getLevel).thenComparing(ClassFetureDto::getName))))
								)
				);
		Collections.sort(features, Comparator.comparing(ClassFetureDto::getLevel));
		model.addAttribute("features", features);
		model.addAttribute("heroClass", heroClass);
		model.addAttribute("archetypeTraits", archetypeTraits);
		model.addAttribute("order", "[[ 1, 'asc' ]]");
		model.addAttribute("selectedArchetypeName", heroClass.getArchetypeName());
		return "fragments/class :: view";
	}

	@GetMapping("/classes/images/{englishName}")
	public String getClassImages(Model model, @PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("images", imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId()));
		return "fragments/class :: images";
	}

	@GetMapping("/classes/spells/{englishName}")
	public String getClassSpells(Model model, @PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("heroClass", heroClass);
		model.addAttribute("schools", MagicSchool.values());
		return "fragments/class_spell :: view";
	}

	@GetMapping("/classes/options/{englishName}")
	public String getClassOption(Model model, @PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("heroClass", heroClass);
		model.addAttribute("requirements", optionRepository.findAlldPrerequisite());
		model.addAttribute("levels", prerequisites);
		return "fragments/class_options :: view";
	}

	@GetMapping("/classes/{englishName}/architype/name")
	@ResponseBody
	public String getArchetypeName(@PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		return heroClass.getArchetypeName();
	}

	@GetMapping("/classes/{englishName}/architypes/list")
	public String getArchetypeList(Model model, @PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("archetypeName", heroClass.getArchetypeName());
		model.addAttribute("archetypes", heroClass.getArchetypes().stream().sorted(Comparator.comparing(Archetype::getBook)).collect(Collectors.toList()));
		model.addAttribute("images", imageRepository.findAllByTypeAndRefId(ImageType.CLASS, heroClass.getId()));
		return "fragments/archetypes_list :: sub_menu";
	}

	@GetMapping("/classes/{className}/architypes/{archetypeName}")
	public String getByClassIdAndByArchetypeId(Model model, @PathVariable String className, @PathVariable String archetypeName) {
		HeroClass heroClass = classRepository.findByEnglishName(className.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		List<ClassFetureDto> features = new ArrayList<>();
		heroClass.getTraits().stream()
			.filter(f -> !f.isArchitype())
			.map(f -> new ClassFetureDto(f, heroClass.getName()))
			.forEach(f -> features.add(f));
		Archetype archetype = heroClass.getArchetypes()
				.stream()
				.filter(a -> archetypeName.replace('_', ' ').equalsIgnoreCase(a.getEnglishName()))
				.findFirst().orElseGet(Archetype::new);

		ClassFetureDto feature = new ClassFetureDto();
		feature.setId(archetype.getId());
		feature.setLevel(archetype.getLevel());
		feature.setDescription(archetype.getDescription());
		feature.setName(archetype.getName());
		feature.setPrefix("ad");
		if (archetype.getBook() != null) {
			feature.setType(heroClass.getArchetypeName() + ". Источник: " + archetype.getBook().getName()
					+ (archetype.getPage() == null ? "" : ", стр. " + archetype.getPage()));
		}
		features.add(feature);
		archetype.getFeats().stream()
			.map(f -> new ClassFetureDto(f, archetype.getGenitiveName()))
			.forEach(f -> features.add(f));

		Collections.sort(features, Comparator.comparing(ClassFetureDto::getLevel).thenComparing(ClassFetureDto::getOrder));
		model.addAttribute("archetypeName", archetype.getName());

		model.addAttribute("heroClass", heroClass);
		model.addAttribute("features", features);
		model.addAttribute("selectedArchetypeId", archetype.getId());
		model.addAttribute("selectedArchetype", archetype);
		model.addAttribute("selectedArchetypeName", archetype.getName());
		model.addAttribute("archetypeSpells", archetype.getSpells().stream().filter(s-> s.getLevel() != 0).collect(Collectors.toList()));
		return "fragments/archetype :: view";
	}

	@GetMapping("/classes/{name}/description")
	@ResponseBody
	public String getClassDescription(@PathVariable String name) {
		HeroClass heroClass = classRepository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		return heroClass.getDescription();
	}

	@GetMapping("/classes/{className}/archetype/{archetypeName}/description")
	@ResponseBody
	public String getArchetypeDescription(@PathVariable String className, @PathVariable String archetypeName) {
		HeroClass heroClass = classRepository.findByEnglishName(className.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		return heroClass.getArchetypes()
			.stream()
			.filter(a -> a.getEnglishName().equalsIgnoreCase(archetypeName.replace("_", " ")))
			.map(Archetype::getDescription)
			.findFirst().orElse("");
	}

	@GetMapping("/classes/feature/{id}")
	@ResponseBody
	public String getClassFeatureDescription(@PathVariable Integer id) {
		return traitRepository.findById(id).map(HeroClassTrait::getDescription).orElse("");
	}

	@GetMapping("/classes/archetype/feature/{id}")
	@ResponseBody
	public String getArchetypeFeatureDescription(@PathVariable Integer id) {
		return archetypeTraitRepository.findById(id).map(ArchetypeTrait::getDescription).orElse("");
	}
}
