package club.dnd5.portal.controller.api;

import club.dnd5.portal.controller.api.bestiary.Bestiary2ApiController;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.service.BestiaryService;
import club.dnd5.portal.service.TokenBorderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ContentMutationAuthorizationTest.TestConfig.class)
class ContentMutationAuthorizationTest {
	@Autowired
	private Bestiary2ApiController bestiaryController;
	@Autowired
	private TokenApiController tokenController;
	@Autowired
	private TokenBorderApiController tokenBorderController;

	@Test
	@WithMockUser(roles = "USER")
	void contentMutationsRejectRegularUser() {
		assertContentMutationsDenied();
	}

	@Test
	@WithMockUser(roles = "MODERATOR")
	void adminOnlyContentMutationsRejectModerator() {
		assertContentMutationsDenied();
	}

	@Test
	@WithAnonymousUser
	void contentMutationsRejectAnonymousUser() {
		assertContentMutationsDenied();
	}

	private void assertContentMutationsDenied() {
		for (Executable mutation : contentMutations()) {
			assertThrows(AccessDeniedException.class, mutation);
		}
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void contentMutationsAllowAdmin() {
		for (Executable mutation : contentMutations()) {
			assertDoesNotThrow(mutation);
		}
	}

	private Executable[] contentMutations() {
		return new Executable[] {
			() -> bestiaryController.createBeast(null),
			() -> bestiaryController.updateBeast(null),
			() -> tokenController.addToken("Goblin", null, "goblin", "round", "token-url"),
			() -> tokenController.deleteTokenById(1L),
			() -> tokenBorderController.createTokenBorder(null),
			() -> tokenBorderController.updateTokenBorder(null),
			() -> tokenBorderController.deleteTokenBorder(1L),
			() -> tokenBorderController.uploadTokenBorder(null)
		};
	}

	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class TestConfig {
		@Bean
		BestiaryService bestiaryService() {
			return mock(BestiaryService.class);
		}

		@Bean
		TokenRepository tokenRepository() {
			return mock(TokenRepository.class);
		}

		@Bean
		BestiaryRepository bestiaryRepository() {
			BestiaryRepository repository = mock(BestiaryRepository.class);
			Creature creature = mock(Creature.class);
			when(creature.getId()).thenReturn(1);
			when(repository.findByEnglishName("goblin")).thenReturn(Optional.of(creature));
			return repository;
		}

		@Bean
		TokenBorderServiceImpl tokenBorderService() {
			return mock(TokenBorderServiceImpl.class);
		}

		@Bean
		Bestiary2ApiController bestiaryController(BestiaryService bestiaryService) {
			return new Bestiary2ApiController(bestiaryService);
		}

		@Bean
		TokenApiController tokenController(TokenRepository tokenRepository, BestiaryRepository bestiaryRepository) {
			return new TokenApiController(tokenRepository, bestiaryRepository);
		}

		@Bean
		TokenBorderApiController tokenBorderController(TokenBorderServiceImpl tokenBorderService) {
			return new TokenBorderApiController(tokenBorderService);
		}
	}
}
