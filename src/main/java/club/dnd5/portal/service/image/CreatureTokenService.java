package club.dnd5.portal.service.image;

import club.dnd5.portal.dto.api.image.ImageTokenApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.token.Token;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

/**
 * Круглые токены существ (таблица {@code tokens}, тип {@link #ROUND_TYPE}).
 * Карточка существа показывает токен первой картинкой, перед галереей.
 * Файл лежит по английскому названию — {@code tokens/round/<name>.webp},
 * а адрес с меткой версии хранится в токене.
 */
@Service
@RequiredArgsConstructor
public class CreatureTokenService {
	/** Тип токена, который показывает карточка существа. */
	public static final String ROUND_TYPE = "круглый";

	/** Токен перезаписывают на месте, поэтому кэшируем его ненадолго. */
	private static final String TOKEN_CACHE_CONTROL = "public, max-age=600";

	private final TokenRepository tokenRepository;
	private final BestiaryRepository bestiaryRepository;
	private final ImageStorageService imageStorageService;

	/**
	 * Путь круглого токена от корня картинок сайта: так же их раскладывает
	 * {@code TokenApiController#addToken}.
	 * @param urlName адресное имя существа
	 */
	static String roundPath(String urlName) {
		return "tokens/round/" + urlName.replace("'", "-").replaceAll("[(),]+", "_") + ".webp";
	}

	/**
	 * Токен существа.
	 * @param creatureId идентификатор существа
	 */
	@Transactional(readOnly = true)
	public ImageTokenApi get(Integer creatureId) {
		requireCreature(creatureId);
		return new ImageTokenApi(tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(creatureId, ROUND_TYPE)
			.map(Token::getUrl)
			.orElse(null));
	}

	/**
	 * Загружает токен существа, перезаписывая прежний файл.
	 * @param creatureId идентификатор существа
	 * @param file загруженный файл
	 * @return токен; к адресу добавлена метка версии, чтобы браузер не показал старый
	 */
	@Transactional
	public ImageTokenApi upload(Integer creatureId, MultipartFile file) {
		Creature creature = requireCreature(creatureId);
		String url = imageStorageService.uploadTo(roundPath(creature.getUrlName()), file, TOKEN_CACHE_CONTROL)
			+ "?v=" + Instant.now().getEpochSecond();
		Token token = tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(creatureId, ROUND_TYPE)
			.orElseGet(() -> newToken(creature));
		token.setUrl(url);
		tokenRepository.saveAndFlush(token);
		return new ImageTokenApi(url);
	}

	/**
	 * Удаляет токен существа: запись и файл, если он лежит в хранилище сайта.
	 * @param creatureId идентификатор существа
	 */
	@Transactional
	public void delete(Integer creatureId) {
		requireCreature(creatureId);
		tokenRepository.findFirstByRefIdAndTypeOrderByIdAsc(creatureId, ROUND_TYPE).ifPresent(token -> {
			if (imageStorageService.pathOf(token.getUrl()) != null) {
				imageStorageService.delete(token.getUrl());
			}
			tokenRepository.delete(token);
			tokenRepository.flush();
		});
	}

	private static Token newToken(Creature creature) {
		Token token = new Token();
		token.setRefId(creature.getId());
		token.setName(creature.getName());
		token.setAltName(creature.getAltName());
		token.setEnglishName(creature.getEnglishName());
		token.setType(ROUND_TYPE);
		return token;
	}

	private Creature requireCreature(Integer creatureId) {
		return bestiaryRepository.findById(creatureId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Существо не найдено"));
	}
}
