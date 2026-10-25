package club.dnd5.portal.controller.api.user;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.user.ExcludedSourcesDto;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSourcesControllerTest {
	private final UserRepository userRepository = mock(UserRepository.class);
	private final BookRepository bookRepository = mock(BookRepository.class);
	private final UserSourcesController controller = new UserSourcesController(userRepository, bookRepository);

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setUsername("magistr");
		when(userRepository.findByEmailOrUsername("magistr", "magistr")).thenReturn(Optional.of(user));

		Book phb = book("PHB", TypeBook.OFFICAL);
		Book ua = book("UA", TypeBook.TEST);
		when(bookRepository.findAll()).thenReturn(List.of(phb, ua));
		when(bookRepository.findAllByTypeOrderByYearAscSourceAsc(any())).thenReturn(Collections.emptyList());
		when(bookRepository.findAllByTypeOrderByYearAscSourceAsc(TypeBook.OFFICAL)).thenReturn(List.of(phb));
		when(bookRepository.findAllByTypeOrderByYearAscSourceAsc(TypeBook.TEST)).thenReturn(List.of(ua));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldMarkExcludedBooksAsNotDefault() {
		signIn();
		user.getExcludedSources().add("UA");

		FilterApi filter = controller.getSources().getBody();

		assertThat(filter).isNotNull();
		assertThat(filter.getSources()).extracting(FilterApi::getKey).containsExactly("OFFICAL", "TEST");
		assertThat(filter.getSources().get(0).getValues()).extracting(FilterValueApi::getDefaultValue).containsExactly(true);
		assertThat(filter.getSources().get(1).getValues()).extracting(FilterValueApi::getDefaultValue).containsExactly(false);
	}

	@Test
	void shouldSaveOnlyKnownBooks() {
		signIn();
		user.getExcludedSources().add("PHB");

		ResponseEntity<FilterApi> response = controller.updateSources(request("UA", "UNKNOWN"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(user.getExcludedSources()).containsExactly("UA");
		verify(userRepository).save(user);
	}

	@Test
	void shouldRejectGuest() {
		ResponseEntity<FilterApi> response = controller.updateSources(request("UA"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(userRepository, never()).save(any());
	}

	private void signIn() {
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("magistr", null, Collections.emptyList()));
	}

	private ExcludedSourcesDto request(String... sources) {
		ExcludedSourcesDto request = new ExcludedSourcesDto();
		request.setExcluded(List.of(sources));
		return request;
	}

	private Book book(String source, TypeBook type) {
		Book book = new Book(source);
		book.setName(source + " name");
		book.setType(type);
		return book;
	}
}
