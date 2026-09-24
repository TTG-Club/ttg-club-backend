package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.wiki.GodSaveApi;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.god.Domain;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.god.GodSex;
import club.dnd5.portal.model.god.Pantheon;
import club.dnd5.portal.model.god.Rank;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.GodRepository;
import club.dnd5.portal.repository.datatable.PantheonGodRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GodApiControllerTest {
	@Test
	void shouldUpdateGodAndRecordPreviousState() {
		God god = existingGod();
		Pantheon newPantheon = pantheon(2, "Новый пантеон");
		Book newBook = new Book("SCAG");
		GodRepository godRepository = mock(GodRepository.class);
		PantheonGodRepository pantheonRepository = mock(PantheonGodRepository.class);
		ImageRepository imageRepository = mock(ImageRepository.class);
		BookResolver bookResolver = mock(BookResolver.class);
		AuditService auditService = mock(AuditService.class);
		when(godRepository.findById(1)).thenReturn(Optional.of(god));
		when(godRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(pantheonRepository.findById(2)).thenReturn(Optional.of(newPantheon));
		when(bookResolver.find("SCAG")).thenReturn(Optional.of(newBook));
		when(godRepository.saveAndFlush(any(God.class))).thenAnswer(invocation -> invocation.getArgument(0));

		GodSaveApi request = new GodSaveApi();
		request.setName(" Новое имя ");
		request.setEnglishName(" New god ");
		request.setAltName(" Альтернативное ");
		request.setCommitment(" Магия ");
		request.setSex(GodSex.FEMALE);
		request.setRank(Rank.GREAT);
		request.setAlignment(Alignment.CHAOTIC_GOOD);
		request.setDescription(" Новое описание ");
		request.setAlternativeDescription(" Альтернативное описание ");
		request.setSymbol(" Звезда ");
		request.setNicknames("Первая, Вторая");
		request.setDomains(Arrays.asList(Domain.ARCANA, Domain.LIGHT));
		request.setPantheonId(2);
		request.setSource("SCAG");
		request.setPage((short) 42);

		GodApiController controller = new GodApiController(
			godRepository, pantheonRepository, imageRepository, bookResolver, auditService);
		controller.updateGod(1, request);

		ArgumentCaptor<GodSaveApi> snapshot = ArgumentCaptor.forClass(GodSaveApi.class);
		verify(auditService).record(eq("GOD"), eq(1), eq(RevisionOperation.UPDATE), snapshot.capture());
		assertThat(snapshot.getValue().getName()).isEqualTo("Старое имя");
		assertThat(snapshot.getValue().getDescription()).isEqualTo("Старое\rописание");
		assertThat(snapshot.getValue().getRank()).isEqualTo(Rank.MIDDLE);
		assertThat(god.getName()).isEqualTo("Новое имя");
		assertThat(god.getEnglishName()).isEqualTo("New god");
		assertThat(god.getAltName()).isEqualTo("Альтернативное");
		assertThat(god.getCommitment()).isEqualTo("Магия");
		assertThat(god.getSex()).isEqualTo(GodSex.FEMALE);
		assertThat(god.getRankValue()).isEqualTo(Rank.GREAT);
		assertThat(god.getAligment()).isEqualTo(Alignment.CHAOTIC_GOOD);
		assertThat(god.getRawDescription()).isEqualTo("Новое описание");
		assertThat(god.getAlternativeDescription()).isEqualTo("Альтернативное описание");
		assertThat(god.getSymbol()).isEqualTo("Звезда");
		assertThat(god.getDomains()).containsExactly(Domain.ARCANA, Domain.LIGHT);
		assertThat(god.getPantheon()).isSameAs(newPantheon);
		assertThat(god.getBook()).isSameAs(newBook);
		assertThat(god.getPage()).isEqualTo((short) 42);
	}

	private God existingGod() {
		God god = new God();
		god.setId(1);
		god.setName("Старое имя");
		god.setEnglishName("Old god");
		god.setCommitment("Знания");
		god.setSex(GodSex.MALE);
		god.setRank(Rank.MIDDLE);
		god.setAligment(Alignment.LAWFUL_GOOD);
		god.setDescription("Старое\rописание");
		god.setDomains(Arrays.asList(Domain.KNOWLEDGE));
		god.setPantheon(pantheon(1, "Старый пантеон"));
		god.setBook(new Book("PHB"));
		god.setPage((short) 12);
		return god;
	}

	private Pantheon pantheon(int id, String name) {
		Pantheon pantheon = new Pantheon();
		pantheon.setId(id);
		pantheon.setName(name);
		return pantheon;
	}
}
