package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.classes.ArchetypeEditApi;
import club.dnd5.portal.dto.api.classes.ArchetypeSaveApi;
import club.dnd5.portal.dto.api.classes.FeatureLevelSaveApi;
import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
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
import java.util.Arrays;
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

	@Test
	void updatesArchetypeTableColumns() {
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
		archetype.setHeroClass(heroClass);
		archetype.setName("School of Evocation");
		archetype.setEnglishName("School of Evocation");
		archetype.setDescription("Description");
		archetype.setLevel((byte) 2);
		FeatureLevelDefinition existingColumn = new FeatureLevelDefinition();
		existingColumn.setId(3);
		existingColumn.setName("Old column");
		archetype.setFeatureLevelDefenitions(new java.util.ArrayList<>(Collections.singletonList(existingColumn)));

		FeatureLevelSaveApi tableColumn = new FeatureLevelSaveApi();
		tableColumn.setId(3);
		tableColumn.setName("Sorcery points");
		tableColumn.setPrefix("+");
		tableColumn.setLevels(Arrays.asList(
			(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5,
			(byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10,
			(byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15,
			(byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20));
		ArchetypeSaveApi request = new ArchetypeSaveApi();
		request.setName(archetype.getName());
		request.setEnglishName(archetype.getEnglishName());
		request.setDescription(archetype.getDescription());
		request.setLevel(archetype.getLevel());
		request.setSpellLevelType(ArchetypeSpellLevelType.SPELL_LEVEL);
		request.setTableColumns(Collections.singletonList(tableColumn));

		when(archetypeRepository.findById(2)).thenReturn(Optional.of(archetype));
		when(archetypeRepository.saveAndFlush(archetype)).thenReturn(archetype);
		when(archetypeTraitRepository.findAllByArchetypeId(2)).thenReturn(Collections.emptyList());

		ArchetypeEditApi result = controller.updateArchetype(2, request);

		assertThat(result.getTableColumns()).hasSize(1);
		assertThat(result.getTableColumns().get(0).getName()).isEqualTo("Sorcery points");
		assertThat(archetype.getFeatureLevelDefenitions().get(0).getPrefix()).isEqualTo("+");
		assertThat(archetype.getFeatureLevelDefenitions().get(0).getL20()).isEqualTo((byte) 20);
		assertThat(archetype.getSpellLevelType()).isEqualTo(ArchetypeSpellLevelType.SPELL_LEVEL);
		assertThat(result.getSpellLevelType()).isEqualTo(ArchetypeSpellLevelType.SPELL_LEVEL);
	}
}
