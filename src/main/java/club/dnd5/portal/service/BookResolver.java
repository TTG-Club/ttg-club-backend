package club.dnd5.portal.service;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.repository.datatable.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Поиск книги-источника для сущностей мастерской.
 */
@RequiredArgsConstructor
@Service
public class BookResolver {
	private final BookRepository bookRepository;

	/**
	 * Поиск книги по аббревиатуре (MM) или, для совместимости, по английскому названию.
	 *
	 * @return пустое значение, если источник не указан или книга не найдена
	 */
	public Optional<Book> find(String source) {
		if (!StringUtils.hasText(source)) {
			return Optional.empty();
		}
		String value = source.trim();
		Optional<Book> book = bookRepository.findBySource(value);
		if (book.isPresent()) {
			return book;
		}
		return bookRepository.findByEnglishName(value);
	}

	/**
	 * Книга по умолчанию для самодельного контента.
	 */
	public Book getCustomBook() {
		return bookRepository.findFirstByType(TypeBook.CUSTOM)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CUSTOM source book is not configured"));
	}
}
