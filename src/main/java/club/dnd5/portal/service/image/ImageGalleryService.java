package club.dnd5.portal.service.image;

import club.dnd5.portal.dto.api.image.ImageCoverApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.image.CoverImage;
import club.dnd5.portal.model.image.Image;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.ArchetypeRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.GodRepository;
import club.dnd5.portal.repository.datatable.ItemRepository;
import club.dnd5.portal.repository.datatable.MagicItemRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Галереи записей (таблица {@code images}) и обложки классов и рас.
 */
@Service
@RequiredArgsConstructor
public class ImageGalleryService {
	/** Обложку перезаписывают на месте, поэтому кэшируем её ненадолго. */
	private static final String COVER_CACHE_CONTROL = "public, max-age=600";

	private final ImageRepository imageRepository;
	private final ImageStorageService imageStorageService;
	private final ClassRepository classRepository;
	private final ArchetypeRepository archetypeRepository;
	private final RaceRepository raceRepository;
	private final BestiaryRepository bestiaryRepository;
	private final GodRepository godRepository;
	private final ItemRepository itemRepository;
	private final MagicItemRepository magicItemRepository;
	private final WeaponRepository weaponRepository;
	private final ArmorRepository armorRepository;

	/**
	 * Адреса картинок галереи записи по порядку.
	 * @param type раздел записи
	 * @param id идентификатор записи
	 */
	@Transactional(readOnly = true)
	public List<String> getGallery(ImageType type, Integer id) {
		requireEntry(type, id);
		return new ArrayList<>(imageRepository.findAllByTypeAndRefId(type, id));
	}

	/**
	 * Заменяет галерею записи: порядок списка становится порядком картинок.
	 * Файлы убранных картинок не удаляются — они могут быть общими у нескольких записей.
	 * @param type раздел записи
	 * @param id идентификатор записи
	 * @param urls адреса картинок
	 * @return сохранённая галерея
	 */
	@Transactional
	public List<String> replaceGallery(ImageType type, Integer id, List<String> urls) {
		requireEntry(type, id);
		List<String> unique = new ArrayList<>(new LinkedHashSet<>(urls.stream().map(String::trim).toList()));
		for (String url : unique) {
			if (!url.startsWith("https://") && !url.startsWith("http://")) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Неверный адрес картинки: " + url);
			}
		}
		imageRepository.deleteAllByTypeAndRefId(type, id);
		imageRepository.flush();
		for (String url : unique) {
			Image image = new Image();
			image.setType(type);
			image.setRefId(id);
			image.setUrl(url);
			imageRepository.save(image);
		}
		imageRepository.flush();
		return unique;
	}

	/**
	 * Загружает картинку и добавляет её в конец галереи записи.
	 * @param type раздел записи
	 * @param id идентификатор записи
	 * @param file загруженный файл
	 * @return сохранённая галерея
	 */
	@Transactional
	public List<String> addToGallery(ImageType type, Integer id, MultipartFile file) {
		requireEntry(type, id);
		String url = imageStorageService.upload(type, file);
		Image image = new Image();
		image.setType(type);
		image.setRefId(id);
		image.setUrl(url);
		imageRepository.saveAndFlush(image);
		return new ArrayList<>(imageRepository.findAllByTypeAndRefId(type, id));
	}

	/**
	 * Обложка класса или расы.
	 * @param type {@link ImageType#CLASS} или {@link ImageType#RACE}
	 * @param id идентификатор записи
	 */
	@Transactional(readOnly = true)
	public ImageCoverApi getCover(ImageType type, Integer id) {
		return switch (type) {
			case CLASS -> new ImageCoverApi(CoverImage.HOST + CoverImage.classPath(className(id)), true);
			case RACE -> {
				Race race = raceRepository.findById(id).orElseThrow(ImageGalleryService::notFound);
				Race main = race.getParent() == null ? race : race.getParent();
				yield new ImageCoverApi(CoverImage.HOST + CoverImage.racePath(main.getEnglishName()), race.getParent() == null);
			}
			default -> throw noCover(type);
		};
	}

	/**
	 * Загружает обложку класса или основной расы, перезаписывая прежний файл.
	 * @param type {@link ImageType#CLASS} или {@link ImageType#RACE}
	 * @param id идентификатор записи
	 * @param file загруженный файл
	 * @return обложка; к адресу добавлена метка версии, чтобы браузер не показал старую
	 */
	@Transactional(readOnly = true)
	public ImageCoverApi uploadCover(ImageType type, Integer id, MultipartFile file) {
		String path = switch (type) {
			case CLASS -> CoverImage.classPath(className(id));
			case RACE -> {
				Race race = raceRepository.findById(id).orElseThrow(ImageGalleryService::notFound);
				if (race.getParent() != null) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "У подрасы нет своей обложки: загрузите её основной расе");
				}
				yield CoverImage.racePath(race.getEnglishName());
			}
			default -> throw noCover(type);
		};
		String url = imageStorageService.uploadTo(path, file, COVER_CACHE_CONTROL);
		return new ImageCoverApi(url + "?v=" + Instant.now().getEpochSecond(), true);
	}

	private String className(Integer id) {
		return classRepository.findById(id).orElseThrow(ImageGalleryService::notFound).getEnglishName();
	}

	private static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Запись не найдена");
	}

	private static ApiException noCover(ImageType type) {
		return new ApiException(HttpStatus.BAD_REQUEST, "Обложка есть только у классов и рас, а не у " + type);
	}

	private void requireEntry(ImageType type, Integer id) {
		boolean exists = switch (type) {
			case CLASS -> classRepository.existsById(id);
			case SUBCLASS -> archetypeRepository.existsById(id);
			case RACE -> raceRepository.existsById(id);
			case CREATURE -> bestiaryRepository.existsById(id);
			case GOD -> godRepository.existsById(id);
			case ITEM -> itemRepository.existsById(id);
			case MAGIC_ITEM -> magicItemRepository.existsById(id);
			case WEAPON -> weaponRepository.existsById(id);
			case ARMOR -> armorRepository.existsById(id);
		};
		if (!exists) {
			throw notFound();
		}
	}
}
