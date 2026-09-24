package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.wiki.GodSaveApi;
import club.dnd5.portal.model.Alignment;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = GodApiControllerAuthorizationTest.TestConfig.class)
class GodApiControllerAuthorizationTest {
	@Autowired
	private GodApiController controller;

	@Test
	@WithMockUser(roles = "USER")
	void updateRejectsRegularUser() {
		assertThrows(AccessDeniedException.class, () -> controller.updateGod(1, request()));
	}

	@Test
	@WithAnonymousUser
	void updateRejectsAnonymousUser() {
		assertThrows(AccessDeniedException.class, () -> controller.updateGod(1, request()));
	}

	@Test
	@WithMockUser(roles = "MODERATOR")
	void updateAllowsModerator() {
		assertDoesNotThrow(() -> controller.updateGod(1, request()));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void updateAllowsAdmin() {
		assertDoesNotThrow(() -> controller.updateGod(1, request()));
	}

	private GodSaveApi request() {
		GodSaveApi request = new GodSaveApi();
		request.setName("Новое имя");
		request.setEnglishName("New god");
		request.setSex(GodSex.FEMALE);
		request.setRank(Rank.GREAT);
		request.setAlignment(Alignment.CHAOTIC_GOOD);
		request.setDomains(Collections.singletonList(Domain.LIGHT));
		request.setPantheonId(2);
		return request;
	}

	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class TestConfig {
		@Bean
		GodRepository godRepository() {
			GodRepository repository = mock(GodRepository.class);
			when(repository.findById(1)).thenAnswer(invocation -> Optional.of(existingGod()));
			when(repository.findByEnglishName(any())).thenReturn(Optional.empty());
			when(repository.saveAndFlush(any(God.class))).thenAnswer(invocation -> invocation.getArgument(0));
			return repository;
		}

		@Bean
		PantheonGodRepository pantheonRepository() {
			PantheonGodRepository repository = mock(PantheonGodRepository.class);
			when(repository.findById(2)).thenReturn(Optional.of(pantheon()));
			return repository;
		}

		@Bean
		ImageRepository imageRepository() {
			return mock(ImageRepository.class);
		}

		@Bean
		BookResolver bookResolver() {
			BookResolver resolver = mock(BookResolver.class);
			when(resolver.find(any())).thenReturn(Optional.empty());
			return resolver;
		}

		@Bean
		AuditService auditService() {
			return mock(AuditService.class);
		}

		@Bean
		GodApiController godApiController(GodRepository godRepository,
				PantheonGodRepository pantheonRepository,
				ImageRepository imageRepository,
				BookResolver bookResolver,
				AuditService auditService) {
			return new GodApiController(godRepository, pantheonRepository, imageRepository, bookResolver, auditService);
		}

		private static God existingGod() {
			God god = new God();
			god.setId(1);
			god.setName("Старое имя");
			god.setEnglishName("Old god");
			god.setSex(GodSex.MALE);
			god.setRank(Rank.MIDDLE);
			god.setAligment(Alignment.LAWFUL_GOOD);
			god.setDomains(Collections.singletonList(Domain.KNOWLEDGE));
			god.setPantheon(pantheon());
			return god;
		}

		private static Pantheon pantheon() {
			Pantheon pantheon = new Pantheon();
			pantheon.setId(2);
			pantheon.setName("Пантеон");
			return pantheon;
		}
	}
}
