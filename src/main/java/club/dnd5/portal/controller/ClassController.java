package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Hidden
@Controller
@RequiredArgsConstructor
public class ClassController {
	private final ClassRepository classRepository;
	private final HeroClassTraitRepository traitRepository;
	private final ArchetypeTraitRepository archetypeTraitRepository;

	@GetMapping("/classes/{englishName}/architype/name")
	@ResponseBody
	public String getArchetypeName(@PathVariable String englishName) {
		HeroClass heroClass = classRepository.findByEnglishName(englishName.replace("_", " "))
				.orElseThrow(PageNotFoundException::new);
		return heroClass.getArchetypeName();
	}

	@GetMapping("/classes/{name}/description")
	@ResponseBody
	public String getClassDescription(@PathVariable String name) {
		HeroClass heroClass = classRepository.findByEnglishName(name.replace("_", " "))
				.orElseThrow(PageNotFoundException::new);
		return heroClass.getDescription();
	}

	@GetMapping("/classes/{className}/archetype/{archetypeName}/description")
	@ResponseBody
	public String getArchetypeDescription(@PathVariable String className, @PathVariable String archetypeName) {
		HeroClass heroClass = classRepository.findByEnglishName(className.replace("_", " "))
				.orElseThrow(PageNotFoundException::new);
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
