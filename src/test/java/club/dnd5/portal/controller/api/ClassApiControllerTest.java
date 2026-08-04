package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.classes.ArchetypeEditApi;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeRepository;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassApiControllerTest {
	@Test
	void loadsArchetypeTraitsExplicitlyForEditing() {
		ClassRepository classRepository = mock(ClassRepository.class);
		ArchetypeRepository archetypeRepository = mock(ArchetypeRepository.class);
		ArchetypeTraitRepository archetypeTraitRepository = mock(ArchetypeTraitRepository.class);
		ClassApiController controller = new ClassApiController(
			classRepository,
			archetypeRepository,
			mock(ImageRepository.class),
			mock(BookResolver.class),
			mock(HeroClassTraitRepository.class),
			archetypeTraitRepository,
			mock(AuditService.class));

		HeroClass heroClass = new HeroClass();
		heroClass.setId(1);
		Archetype archetype = new Archetype();
		archetype.setId(2);
		archetype.setName("School of Evocation");
		archetype.setEnglishName("School of Evocation");
		archetype.setDescription("Description");
		archetype.setLevel((byte) 2);
		ArchetypeTrait trait = new ArchetypeTrait();
		trait.setId(3);
		trait.setName("Sculpt Spells");
		trait.setDescription("Description");
		trait.setLevel((byte) 2);

		when(classRepository.findByEnglishName("wizard")).thenReturn(Optional.of(heroClass));
		when(archetypeRepository.findByHeroClassIdAndEnglishNameIgnoreCase(1, "school of evocation"))
			.thenReturn(Optional.of(archetype));
		when(archetypeTraitRepository.findAllByArchetypeId(2)).thenReturn(Collections.singletonList(trait));

		ArchetypeEditApi result = controller.getArchetypeForEdit("wizard", "school_of_evocation");

		assertThat(result.getTraits()).extracting(ArchetypeEditApi.TraitApi::getName)
			.containsExactly("Sculpt Spells");
		verify(archetypeTraitRepository).findAllByArchetypeId(2);
	}
}
