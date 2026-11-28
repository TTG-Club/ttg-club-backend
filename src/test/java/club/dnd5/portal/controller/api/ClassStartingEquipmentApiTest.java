package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.classes.ClassSaveApi;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.StartingEquipmentLine;
import club.dnd5.portal.model.classes.StartingEquipmentOption;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeRepository;
import club.dnd5.portal.repository.classes.ArchetypeTraitRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.HeroClassTraitRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.service.FeatureOptionProjection;
import club.dnd5.portal.service.OptionTypeResolver;
import club.dnd5.portal.service.WorkshopPreview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Стартовое снаряжение класса: мастерская сохраняет и возвращает структуру, карточка класса
 * отдаёт её в {@code traits.startingEquipment} рядом с текстом, неверные данные — 400.
 */
class ClassStartingEquipmentApiTest {
	private static final String UPDATE_URL = "/api/v1/workshop/classes/1";
	private static final String STARTING_EQUIPMENT = """
		[
		  {"options": [
		    {"items": [{"kind": "ITEM", "section": "WEAPON", "url": "/weapons/longbow", "name": "Длинный лук"},
		               {"kind": "ITEM", "section": "ITEM", "url": "/items/arrows", "name": "Стрелы", "quantity": 20}]},
		    {"items": [{"kind": "CHOICE", "name": "Воинское оружие", "category": "MARTIAL_WEAPON", "description": "любое"}],
		     "coins": 5}
		  ]},
		  {"options": [{"items": [{"kind": "ITEM", "name": "Амулет предков"}]}]}
		]""";

	private ClassRepository classRepository;
	private AuditService auditService;
	private HeroClass heroClass;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		classRepository = mock(ClassRepository.class);
		auditService = mock(AuditService.class);
		ClassApiController controller = new ClassApiController(
			classRepository,
			mock(ArchetypeRepository.class),
			mock(ImageRepository.class),
			mock(BookResolver.class),
			mock(HeroClassTraitRepository.class),
			mock(ArchetypeTraitRepository.class),
			auditService,
			mock(WorkshopPreview.class),
			mock(OptionTypeResolver.class),
			mock(FeatureOptionProjection.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new ApiExceptionHandler())
			.build();

		heroClass = baseClass();
		when(classRepository.findById(1)).thenReturn(Optional.of(heroClass));
		// поиск по названию в БД без учёта регистра (collation ci)
		when(classRepository.findByEnglishName("fighter")).thenReturn(Optional.of(heroClass));
		when(classRepository.saveAndFlush(any(HeroClass.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void workshopSavesAndReturnsStartingEquipment() throws Exception {
		mockMvc.perform(patch(UPDATE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(request("\"startingEquipment\": " + STARTING_EQUIPMENT)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.traits.equipment[0]").value("Текст снаряжения"))
			.andExpect(jsonPath("$.traits.startingEquipment.length()").value(2))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[0].kind").value("ITEM"))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[0].section").value("WEAPON"))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[0].url").value("/weapons/longbow"))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[0].quantity").value(1))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[1].quantity").value(20))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[1].coins").value(5))
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[1].items[0].category").value("MARTIAL_WEAPON"))
			.andExpect(jsonPath("$.traits.startingEquipment[1].options[0].items[0].url").isEmpty());

		List<StartingEquipmentLine> saved = heroClass.getStartingEquipment();
		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getOptions().get(1).getItems().get(0).getDescription()).isEqualTo("любое");
		assertThat(saved.get(1).getOptions().get(0).getItems().get(0).getName()).isEqualTo("Амулет предков");

		// карточка класса — тот же ответ, что загружает редактор
		mockMvc.perform(post("/api/v1/classes/fighter"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].items[1].url").value("/items/arrows"))
			.andExpect(jsonPath("$.traits.startingEquipment[1].options[0].items[0].kind").value("ITEM"));
	}

	@Test
	void missingStartingEquipmentKeepsSaved() throws Exception {
		heroClass.setStartingEquipment(List.of(coinsLine(15)));

		mockMvc.perform(patch(UPDATE_URL).contentType(MediaType.APPLICATION_JSON).content(request(null)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.traits.startingEquipment[0].options[0].coins").value(15));

		assertThat(heroClass.getStartingEquipment()).hasSize(1);
	}

	@Test
	void emptyStartingEquipmentClearsSaved() throws Exception {
		heroClass.setStartingEquipment(List.of(coinsLine(15)));

		mockMvc.perform(patch(UPDATE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(request("\"startingEquipment\": []")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.traits.startingEquipment").isEmpty());

		assertThat(heroClass.getStartingEquipment()).isNull();
	}

	@Test
	void invalidStartingEquipmentIsBadRequest() throws Exception {
		heroClass.setStartingEquipment(List.of(coinsLine(15)));
		String invalid = """
			"startingEquipment": [{"options": [{"items": [
			  {"kind": "ITEM", "section": "ARMOR", "url": "/weapons/longbow", "name": "Лук"}]}]}]""";

		mockMvc.perform(patch(UPDATE_URL).contentType(MediaType.APPLICATION_JSON).content(request(invalid)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message")
				.value("Стартовое снаряжение, строка 1, вариант 1, предмет 1: адрес раздела ARMOR должен начинаться с /armors/"));

		assertThat(heroClass.getStartingEquipment().get(0).getOptions().get(0).getCoins()).isEqualTo(15);
		verify(classRepository, never()).saveAndFlush(any(HeroClass.class));
	}

	@Test
	void revisionSnapshotIncludesStartingEquipment() throws Exception {
		heroClass.setStartingEquipment(List.of(coinsLine(15)));

		mockMvc.perform(patch(UPDATE_URL).contentType(MediaType.APPLICATION_JSON)
				.content(request("\"startingEquipment\": []")))
			.andExpect(status().isOk());

		ArgumentCaptor<Object> snapshot = ArgumentCaptor.forClass(Object.class);
		verify(auditService).record(any(), any(), any(), snapshot.capture());
		assertThat(((ClassSaveApi) snapshot.getValue()).getStartingEquipment()).hasSize(1);
	}

	private static String request(String extraField) {
		return """
			{"name": "Воин", "englishName": "Fighter", "description": ["Описание"], "diceHp": 10,
			 "equipment": ["Текст снаряжения"]%s}""".formatted(extraField == null ? "" : ", " + extraField);
	}

	private static StartingEquipmentLine coinsLine(int coins) {
		StartingEquipmentOption option = new StartingEquipmentOption();
		option.setCoins(coins);
		StartingEquipmentLine line = new StartingEquipmentLine();
		line.getOptions().add(option);
		return line;
	}

	private static HeroClass baseClass() {
		Book book = new Book("TEST");
		book.setName("Test Book");
		book.setType(TypeBook.OFFICAL);
		HeroClass heroClass = new HeroClass();
		heroClass.setId(1);
		heroClass.setName("Воин");
		heroClass.setEnglishName("Fighter");
		heroClass.setDescription(new ArrayList<>(List.of("Описание")));
		heroClass.setDiceHp((byte) 10);
		heroClass.setBook(book);
		heroClass.setSpellcasterType(SpellcasterType.NONE);
		heroClass.setAvailableSkills(new ArrayList<>());
		heroClass.setPrimaryAbilities(new ArrayList<>());
		heroClass.setFeatureLevelDefenitions(new ArrayList<>());
		heroClass.setLevelDefenitions(new ArrayList<>());
		heroClass.setTraits(Collections.emptyList());
		heroClass.setArchetypes(Collections.emptyList());
		return heroClass;
	}
}
