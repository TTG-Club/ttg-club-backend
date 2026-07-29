package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.wiki.ScreenSaveApi;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

		ScreenApiController controller = new ScreenApiController(screenRepository, bookResolver, auditService);
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
