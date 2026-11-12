package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.wiki.ScreenApi;
import club.dnd5.portal.dto.api.wiki.ScreenDetailApi;
import club.dnd5.portal.dto.api.wiki.ScreenFilter;
import club.dnd5.portal.dto.api.wiki.ScreenRequestApi;
import club.dnd5.portal.dto.api.wiki.ScreenSaveApi;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.service.WorkshopPreview;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScreenApiControllerTest {
	@Test
	void shouldUpdateScreenAndRecordPreviousState() {
		Screen parent = new Screen();
		parent.setId(2);
		parent.setName("Родительский раздел");
		parent.setEnglishName("Parent screen");
		parent.setChields(Collections.emptyList());
		Screen screen = existingScreen();
		ScreenRepository screenRepository = mock(ScreenRepository.class);
		AuditService auditService = mock(AuditService.class);
		BookResolver bookResolver = mock(BookResolver.class);
		when(screenRepository.findById(1)).thenReturn(Optional.of(screen));
		when(screenRepository.findById(2)).thenReturn(Optional.of(parent));
		when(screenRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(screenRepository.saveAndFlush(any(Screen.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ScreenSaveApi request = new ScreenSaveApi();
		request.setName(" Новый раздел ");
		request.setEnglishName(" New screen ");
		request.setAltName(" Альтернативное ");
		request.setCategory(" Правила ");
		request.setDescription(" Новое описание ");
		request.setIcon(" book ");
		request.setOrder(20);
		request.setParentId(2);

		ScreenApiController controller = new ScreenApiController(screenRepository, bookResolver, auditService, mock(WorkshopPreview.class));
		controller.updateScreen(1, request);

		ArgumentCaptor<ScreenSaveApi> snapshot = ArgumentCaptor.forClass(ScreenSaveApi.class);
		verify(auditService).record(eq("SCREEN"), eq(1), eq(RevisionOperation.UPDATE), snapshot.capture());
		assertThat(snapshot.getValue().getName()).isEqualTo("Старый раздел");
		assertThat(snapshot.getValue().getEnglishName()).isEqualTo("Old screen");
		assertThat(snapshot.getValue().getOrder()).isEqualTo(10);
		assertThat(screen.getName()).isEqualTo("Новый раздел");
		assertThat(screen.getEnglishName()).isEqualTo("New screen");
		assertThat(screen.getAltName()).isEqualTo("Альтернативное");
		assertThat(screen.getCategory()).isEqualTo("Правила");
		assertThat(screen.getDescription()).isEqualTo("Новое описание");
		assertThat(screen.getIcon()).isEqualTo("book");
		assertThat(screen.getOrdering()).isEqualTo(20);
		assertThat(screen.getParent()).isSameAs(parent);
	}

	@Test
	void shouldListChildScreensWithoutSearch() {
		ScreenRepository screenRepository = repositoryReturning(Collections.emptyList());

		controller(screenRepository).getScreens(new ScreenRequestApi());

		Root<Screen> root = mock(Root.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> parent = mock(Path.class);
		when(root.get("parent")).thenReturn(parent);

		capturedSpecification(screenRepository).toPredicate(root, mock(CriteriaQuery.class), cb);

		verify(cb).isNotNull(parent);
		verify(cb, never()).isNull(any());
	}

	@Test
	void shouldFilterScreensByParentWithoutBooks() {
		ScreenRepository screenRepository = repositoryReturning(Collections.emptyList());
		ScreenFilter filter = new ScreenFilter();
		filter.setParents(Arrays.asList(3, 5));
		ScreenRequestApi request = new ScreenRequestApi();
		request.setFilter(filter);

		controller(screenRepository).getScreens(request);

		Root<Screen> root = mock(Root.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path<Object> parent = mock(Path.class);
		Path<Object> parentId = mock(Path.class);
		Predicate notNull = mock(Predicate.class);
		Predicate in = mock(Predicate.class);
		when(root.get("parent")).thenReturn(parent);
		when(parent.get("id")).thenReturn(parentId);
		when(cb.isNotNull(parent)).thenReturn(notNull);
		when(parentId.in(filter.getParents())).thenReturn(in);

		capturedSpecification(screenRepository).toPredicate(root, mock(CriteriaQuery.class), cb);

		verify(parentId).in(Arrays.asList(3, 5));
		verify(root, never()).join(any(String.class), any());
	}

	@Test
	void shouldMapChildScreenWithParentReference() {
		Screen root = existingScreen();
		Screen child = new Screen();
		child.setId(3);
		child.setName("Дочерний");
		child.setEnglishName("Child screen");
		child.setParent(root);
		child.setBook(book());
		ScreenRepository screenRepository = repositoryReturning(Collections.singletonList(child));

		List<ScreenApi> screens = controller(screenRepository).getScreens(new ScreenRequestApi());

		ScreenApi parent = screens.get(0).getParent();
		assertThat(parent.getName().getRus()).isEqualTo("Старый раздел");
		assertThat(parent.getUrl()).isEqualTo("/screens/old_screen");
		assertThat(parent.getOrder()).isEqualTo(10);
		assertThat(parent.getParent()).isNull();
		assertThat(parent.getSource()).isNull();
	}

	@Test
	void shouldKeepScreenDetailJsonShape() {
		Screen root = existingScreen();
		Screen child = new Screen();
		child.setId(3);
		child.setName("Дочерний");
		child.setEnglishName("Child screen");
		child.setDescription("Описание");
		child.setParent(root);
		child.setBook(book());
		child.setChields(Collections.emptyList());
		root.setChields(Collections.singletonList(child));
		ObjectMapper mapper = new ObjectMapper();

		JsonNode childJson = mapper.readTree(mapper.writeValueAsString(new ScreenDetailApi(child)));
		JsonNode rootJson = mapper.readTree(mapper.writeValueAsString(new ScreenDetailApi(root)));

		assertThat(childJson.get("parent").get("id").asInt()).isEqualTo(1);
		assertThat(childJson.get("parent").get("url").asText()).isEqualTo("/screens/old_screen");
		assertThat(childJson.get("parent").has("chields")).isFalse();
		assertThat(rootJson.has("parent")).isFalse();
		assertThat(rootJson.get("chields").get(0).has("parent")).isFalse();
		assertThat(rootJson.get("chields").get(0).get("url").asText()).isEqualTo("/screens/child_screen");
	}

	@Test
	void shouldExposeRootScreensAsParentFilter() {
		ScreenRepository screenRepository = mock(ScreenRepository.class);
		Screen root = existingScreen();
		when(screenRepository.findBook(any())).thenReturn(Collections.emptyList());
		when(screenRepository.findAllByParentIsNullOrderByOrdering()).thenReturn(Collections.singletonList(root));

		FilterApi filter = controller(screenRepository).getFilter();

		assertThat(filter.getOther()).hasSize(1);
		FilterApi parentFilter = filter.getOther().get(0);
		assertThat(parentFilter.getKey()).isEqualTo("parent");
		assertThat(parentFilter.getName()).isEqualTo("Разделы");
		FilterValueApi value = parentFilter.getValues().get(0);
		assertThat(value.getLabel()).isEqualTo("Старый раздел");
		assertThat(value.getKey()).isEqualTo(1);
		assertThat(filter.getSources()).isEmpty();
	}

	private ScreenRepository repositoryReturning(List<Screen> screens) {
		ScreenRepository screenRepository = mock(ScreenRepository.class);
		Page<Screen> page = new PageImpl<>(screens);
		when(screenRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
		return screenRepository;
	}

	private Specification<Screen> capturedSpecification(ScreenRepository screenRepository) {
		ArgumentCaptor<Specification<Screen>> captor = ArgumentCaptor.forClass(Specification.class);
		verify(screenRepository).findAll(captor.capture(), any(Pageable.class));
		return captor.getValue();
	}

	private ScreenApiController controller(ScreenRepository screenRepository) {
		return new ScreenApiController(screenRepository, mock(BookResolver.class), mock(AuditService.class), mock(WorkshopPreview.class));
	}

	private Book book() {
		Book book = new Book();
		book.setSource("PHB");
		book.setName("Книга игрока");
		book.setType(TypeBook.OFFICAL);
		return book;
	}

	private Screen existingScreen() {
		Screen screen = new Screen();
		screen.setId(1);
		screen.setName("Старый раздел");
		screen.setEnglishName("Old screen");
		screen.setDescription("Старое описание");
		screen.setOrdering(10);
		screen.setChields(Collections.emptyList());
		return screen;
	}
}
