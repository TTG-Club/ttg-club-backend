package club.dnd5.portal.service.image;

import club.dnd5.portal.dto.api.image.ImageCoverApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.image.CoverImage;
import club.dnd5.portal.repository.datatable.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

/**
 * Обложки источников: файл лежит по аббревиатуре книги
 * ({@link CoverImage#bookPath(String)}), а адрес с меткой версии хранится
 * в самой книге, чтобы список знал, у каких книг обложка есть.
 */
@Service
@RequiredArgsConstructor
public class BookCoverService {
	/** Обложку перезаписывают на месте, поэтому кэшируем её ненадолго. */
	private static final String COVER_CACHE_CONTROL = "public, max-age=600";

	private final BookRepository bookRepository;
	private final ImageStorageService imageStorageService;

	/**
	 * Загружает обложку источника, перезаписывая прежний файл.
	 * @param source аббревиатура источника
	 * @param file загруженный файл
	 * @return обложка; к адресу добавлена метка версии, чтобы браузер не показал старую
	 */
	@Transactional
	public ImageCoverApi upload(String source, MultipartFile file) {
		Book book = find(source);
		String url = imageStorageService.uploadTo(CoverImage.bookPath(book.getSource()), file, COVER_CACHE_CONTROL);
		book.setCover(url + "?v=" + Instant.now().getEpochSecond());
		bookRepository.saveAndFlush(book);
		return new ImageCoverApi(book.getCover(), true);
	}

	/**
	 * Удаляет обложку источника: файл из хранилища и адрес из книги.
	 * @param source аббревиатура источника
	 */
	@Transactional
	public void delete(String source) {
		Book book = find(source);
		if (book.getCover() == null) {
			return;
		}
		imageStorageService.delete(book.getCover());
		book.setCover(null);
		bookRepository.saveAndFlush(book);
	}

	private Book find(String source) {
		return bookRepository.findById(source)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Источник не найден"));
	}
}
