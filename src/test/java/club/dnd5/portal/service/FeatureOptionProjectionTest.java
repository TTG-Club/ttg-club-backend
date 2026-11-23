package club.dnd5.portal.service;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.FeatureOption;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.OptionTypeDefinition;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.repository.classes.OptionTypeDefinitionRepository;
import club.dnd5.portal.repository.datatable.OptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeatureOptionProjectionTest {
	private OptionRepository optionRepository;
	private HeroClassTraitRepository heroClassTraitRepository;
	private FeatureOptionProjection projection;
	private OptionTypeDefinition invocations;
	private OptionTypeDefinition styles;

	@BeforeEach
	void setUp() {
		optionRepository = mock(OptionRepository.class);
		OptionTypeDefinitionRepository typeRepository = mock(OptionTypeDefinitionRepository.class);
		heroClassTraitRepository = mock(HeroClassTraitRepository.class);
		ArchetypeTraitRepository archetypeTraitRepository = mock(ArchetypeTraitRepository.class);
		BookResolver bookResolver = mock(BookResolver.class);
		invocations = type("ELDRITCH_INVOCATION");
		styles = type("FIGHTING_STYLE");
		when(typeRepository.findById("ELDRITCH_INVOCATION")).thenReturn(Optional.of(invocations));
		when(bookResolver.find(any())).thenReturn(Optional.empty());
		when(optionRepository.findByEnglishName(anyString())).thenReturn(Optional.empty());
		when(heroClassTraitRepository.findFirstByOptionsType(anyString())).thenReturn(Optional.empty());
		when(archetypeTraitRepository.findFirstByOptionsType(anyString())).thenReturn(Optional.empty());
		projection = new FeatureOptionProjection(optionRepository, typeRepository, heroClassTraitRepository,
			archetypeTraitRepository, bookResolver);
	}

	private static OptionTypeDefinition type(String code) {
		OptionTypeDefinition type = new OptionTypeDefinition();
		type.setCode(code);
		type.setName(code);
		return type;
	}

	private static FeatureOption option(String name, String englishName) {
		FeatureOption option = new FeatureOption();
		option.setName(name);
		option.setEnglishName(englishName);
		option.setDescription(new ArrayList<>(List.of("Текст")));
		return option;
	}

	private static Option entity(String englishName, OptionTypeDefinition... types) {
		Option option = new Option();
		option.setName(englishName);
		option.setEnglishName(englishName);
		option.setOptionTypes(new ArrayList<>(Arrays.asList(types)));
		return option;
	}

	@Test
	void normalizeDropsBlankAndDuplicateOptions() {
		FeatureOption duplicate = option("Копия", "agonizing blast");
		FeatureOption blank = option(" ", "Blank");
		FeatureOption first = option(" Мучительный взрыв ", " Agonizing Blast ");
		first.setPrerequisite("  ");

		List<FeatureOption> result = FeatureOptionProjection.normalize(
			new ArrayList<>(Arrays.asList(first, null, blank, duplicate)));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Мучительный взрыв");
		assertThat(result.get(0).getEnglishName()).isEqualTo("Agonizing Blast");
		assertThat(result.get(0).getPrerequisite()).isNull();
	}

	@Test
	void syncCreatesAndUpdatesSectionRecords() {
		Book book = new Book("PHB");
		Option existing = entity("Agonizing Blast", invocations);
		when(optionRepository.findByEnglishName("Agonizing Blast")).thenReturn(Optional.of(existing));
		when(optionRepository.findAllByType(invocations)).thenReturn(List.of(existing));

		FeatureOption updated = option("Мучительный взрыв", "Agonizing Blast");
		updated.setPrerequisite("Заклинание «Мистический заряд»");
		projection.sync("ELDRITCH_INVOCATION", List.of(updated, option("Маска многих лиц", "Mask of Many Faces")), book);

		assertThat(existing.getName()).isEqualTo("Мучительный взрыв");
		assertThat(existing.getPrerequisite()).isEqualTo("Заклинание «Мистический заряд»");
		verify(optionRepository).save(existing);
		verify(optionRepository, never()).delete(any(Option.class));
	}

	@Test
	void syncRemovesTypeFromDroppedOptions() {
		Option onlyHere = entity("Old Invocation", invocations);
		Option shared = entity("Defense", invocations, styles);
		when(optionRepository.findAllByType(invocations)).thenReturn(List.of(onlyHere, shared));

		projection.sync("ELDRITCH_INVOCATION", List.of(), null);

		verify(optionRepository).delete(onlyHere);
		assertThat(shared.getOptionTypes()).containsExactly(styles);
		verify(optionRepository).save(shared);
	}

	@Test
	void syncWithoutTypeDoesNothing() {
		projection.sync(null, List.of(option("А", "A")), null);
		projection.sync("UNKNOWN", List.of(option("А", "A")), null);

		verify(optionRepository, never()).save(any());
	}

	@Test
	void importMapsSectionRecordsToFeatureOptions() {
		Option second = entity("B Second", invocations);
		second.setLevel(5);
		second.setPrerequisite("Нет");
		Option first = entity("A First", invocations);
		first.setPrerequisite("Пакт клинка");
		when(optionRepository.findAllByType(invocations)).thenReturn(List.of(second, first));

		List<FeatureOption> result = projection.importFromSection(invocations);

		assertThat(result).extracting(FeatureOption::getEnglishName).containsExactly("A First", "B Second");
		assertThat(result.get(0).getPrerequisite()).isEqualTo("Пакт клинка");
		assertThat(result.get(1).getPrerequisite()).isNull();
	}

	@Test
	void findsManagingClassFeature() {
		HeroClass warlock = new HeroClass();
		warlock.setEnglishName("Warlock");
		HeroClassTrait trait = new HeroClassTrait();
		trait.setId(33);
		trait.setName("Таинственные воззвания");
		trait.setHeroClass(warlock);
		when(heroClassTraitRepository.findFirstByOptionsType("ELDRITCH_INVOCATION")).thenReturn(Optional.of(trait));

		Optional<FeatureOptionProjection.ManagingFeature> feature =
			projection.findManagingFeature(entity("Agonizing Blast", invocations));

		assertThat(feature).isPresent();
		assertThat(feature.get().url()).isEqualTo("/classes/warlock#c33");
		assertThat(projection.findManagingFeature(entity("Defense", styles))).isEmpty();
	}
}
