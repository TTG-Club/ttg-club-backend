package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.book.BookEditApi;
import club.dnd5.portal.dto.api.book.BookSaveApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.repository.datatable.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookApiControllerTest {
	@Test
	void shouldUpdateBookAndKeepSource() {
		Book book = existingBook();
		BookRepository bookRepository = mock(BookRepository.class);
		when(bookRepository.findById("PHB")).thenReturn(Optional.of(book));
		when(bookRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(bookRepository.saveAndFlush(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookEditApi saved = new BookApiController(bookRepository).updateBook("PHB", request("Player's Handbook 2014"));

		assertThat(saved.getSource()).isEqualTo("PHB");
		assertThat(book.getName()).isEqualTo("Книга игрока");
		assertThat(book.getEnglishName()).isEqualTo("Player's Handbook 2014");
		assertThat(book.getAltName()).isNull();
		assertThat(book.getType()).isEqualTo(TypeBook.SETTING);
		assertThat(book.getYear()).isEqualTo(2014);
		assertThat(book.getDescription()).isEqualTo("<p>Описание</p>");
	}

	@Test
	void shouldRejectEnglishNameOfAnotherBook() {
		BookRepository bookRepository = mock(BookRepository.class);
		Book other = new Book("DMG");
		when(bookRepository.findById("PHB")).thenReturn(Optional.of(existingBook()));
		when(bookRepository.findByEnglishName("Dungeon Master's Guide")).thenReturn(Optional.of(other));

		BookApiController controller = new BookApiController(bookRepository);

		assertThrows(ResponseStatusException.class,
			() -> controller.updateBook("PHB", request("Dungeon Master's Guide")));
	}

	private BookSaveApi request(String englishName) {
		BookSaveApi request = new BookSaveApi();
		request.setName(" Книга игрока ");
		request.setEnglishName(englishName);
		request.setAltName(" ");
		request.setType(TypeBook.SETTING);
		request.setYear(2014);
		request.setDescription(" <p>Описание</p> ");
		return request;
	}

	private Book existingBook() {
		Book book = new Book("PHB");
		book.setName("Старое название");
		book.setEnglishName("Player's Handbook");
		book.setType(TypeBook.OFFICAL);
		return book;
	}
}
