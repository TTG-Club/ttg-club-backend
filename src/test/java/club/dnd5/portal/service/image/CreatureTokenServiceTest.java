package club.dnd5.portal.service.image;

import club.dnd5.portal.dto.api.image.ImageTokenApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.token.Token;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatureTokenServiceTest {
	private final TokenRepository tokenRepository = mock(TokenRepository.class);
	private final BestiaryRepository bestiaryRepository = mock(BestiaryRepository.class);
	private final ImageStorageService imageStorageService = mock(ImageStorageService.class);
	private final CreatureTokenService service =
		new CreatureTokenService(tokenRepository, bestiaryRepository, imageStorageService);

	private static Creature creature() {
		Creature creature = new Creature();
		creature.setId(7);
		creature.setName("Гоблин");
		creature.setEnglishName("Goblin");
		return creature;
	}

	@Test
	void roundPathMatchesOldTokens() {
		assertThat(CreatureTokenService.roundPath("dragon_(red),_young")).isEqualTo("tokens/round/dragon__red__young.webp");
		assertThat(CreatureTokenService.roundPath("will-o'-wisp")).isEqualTo("tokens/round/will-o--wisp.webp");
	}

	@Test
	void getWithoutTokenReturnsNullUrl() {
		when(bestiaryRepository.findById(7)).thenReturn(Optional.of(creature()));
		when(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(7, CreatureTokenService.ROUND_TYPE))
			.thenReturn(Optional.empty());

		assertThat(service.get(7).url()).isNull();
	}

	@Test
	void uploadCreatesTokenWithVersionedUrl() {
		Creature creature = creature();
		MultipartFile file = mock(MultipartFile.class);
		when(bestiaryRepository.findById(7)).thenReturn(Optional.of(creature));
		when(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(7, CreatureTokenService.ROUND_TYPE))
			.thenReturn(Optional.empty());
		when(imageStorageService.uploadTo(CreatureTokenService.roundPath(creature.getUrlName()), file, "public, max-age=600"))
			.thenReturn("/s3/5e14/tokens/round/goblin.webp");

		ImageTokenApi token = service.upload(7, file);

		ArgumentCaptor<Token> saved = ArgumentCaptor.forClass(Token.class);
		verify(tokenRepository).saveAndFlush(saved.capture());
		assertThat(token.url()).startsWith("/s3/5e14/tokens/round/goblin.webp?v=");
		assertThat(saved.getValue().getUrl()).isEqualTo(token.url());
		assertThat(saved.getValue().getRefId()).isEqualTo(7);
		assertThat(saved.getValue().getType()).isEqualTo(CreatureTokenService.ROUND_TYPE);
		assertThat(saved.getValue().getEnglishName()).isEqualTo("Goblin");
	}

	@Test
	void uploadReplacesUrlOfExistingToken() {
		Token existing = new Token();
		existing.setUrl("https://img.ttg.club/tokens/round/goblin.webp");
		MultipartFile file = mock(MultipartFile.class);
		when(bestiaryRepository.findById(7)).thenReturn(Optional.of(creature()));
		when(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(7, CreatureTokenService.ROUND_TYPE))
			.thenReturn(Optional.of(existing));
		when(imageStorageService.uploadTo(anyString(), eq(file), anyString()))
			.thenReturn("/s3/5e14/tokens/round/goblin.webp");

		service.upload(7, file);

		verify(tokenRepository).saveAndFlush(existing);
		assertThat(existing.getUrl()).startsWith("/s3/5e14/tokens/round/goblin.webp?v=");
	}

	@Test
	void deleteRemovesStoredFileAndToken() {
		Token existing = new Token();
		existing.setUrl("/s3/5e14/tokens/round/goblin.webp?v=1");
		when(bestiaryRepository.findById(7)).thenReturn(Optional.of(creature()));
		when(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(7, CreatureTokenService.ROUND_TYPE))
			.thenReturn(Optional.of(existing));
		when(imageStorageService.pathOf(existing.getUrl())).thenReturn("tokens/round/goblin.webp");

		service.delete(7);

		verify(imageStorageService).delete(existing.getUrl());
		verify(tokenRepository).delete(existing);
	}

	@Test
	void deleteKeepsForeignFile() {
		Token existing = new Token();
		existing.setUrl("https://example.com/goblin.webp");
		when(bestiaryRepository.findById(7)).thenReturn(Optional.of(creature()));
		when(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(7, CreatureTokenService.ROUND_TYPE))
			.thenReturn(Optional.of(existing));

		service.delete(7);

		verify(imageStorageService, never()).delete(anyString());
		verify(tokenRepository).delete(existing);
	}

	@Test
	void unknownCreatureIsNotFound() {
		when(bestiaryRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ApiException.class, () -> service.get(99));
	}
}
