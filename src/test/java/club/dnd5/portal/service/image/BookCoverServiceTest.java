package club.dnd5.portal.service.image;

import club.dnd5.portal.dto.api.image.ImageCoverApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.repository.datatable.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookCoverServiceTest {
	private final BookRepository bookRepository = mock(BookRepository.class);
	private final ImageStorageService imageStorageService = mock(ImageStorageService.class);
	private final BookCoverService service = new BookCoverService(bookRepository, imageStorageService);

	@Test
	void uploadStoresVersionedUrlByAbbreviation() {
		Book book = new Book("UA: Spells");
		MultipartFile file = mock(MultipartFile.class);
		when(bookRepository.findById("UA: Spells")).thenReturn(Optional.of(book));
		when(imageStorageService.uploadTo("books/covers/ua-spells.webp", file, "public, max-age=600"))
			.thenReturn("/s3/5e14/books/covers/ua-spells.webp");

		ImageCoverApi cover = service.upload("UA: Spells", file);

		assertThat(cover.url()).startsWith("/s3/5e14/books/covers/ua-spells.webp?v=");
		assertThat(book.getCover()).isEqualTo(cover.url());
		verify(bookRepository).saveAndFlush(book);
	}

	@Test
	void deleteRemovesFileAndClearsBook() {
		Book book = new Book("PHB");
		book.setCover("/s3/5e14/books/covers/phb.webp?v=1");
		when(bookRepository.findById("PHB")).thenReturn(Optional.of(book));

		service.delete("PHB");

		verify(imageStorageService).delete("/s3/5e14/books/covers/phb.webp?v=1");
		assertThat(book.getCover()).isNull();
	}

	@Test
	void deleteWithoutCoverDoesNothing() {
		when(bookRepository.findById("PHB")).thenReturn(Optional.of(new Book("PHB")));

		service.delete("PHB");

		verify(imageStorageService, never()).delete(anyString());
		verify(bookRepository, never()).saveAndFlush(any());
	}

	@Test
	void unknownBookIsNotFound() {
		when(bookRepository.findById("XXX")).thenReturn(Optional.empty());

		assertThrows(ApiException.class, () -> service.delete("XXX"));
	}
}
