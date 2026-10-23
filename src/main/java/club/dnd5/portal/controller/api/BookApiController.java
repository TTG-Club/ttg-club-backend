package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.BookApi;
import club.dnd5.portal.dto.api.BookRequestApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.book.BookEditApi;
import club.dnd5.portal.dto.api.book.BookSaveApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import jakarta.persistence.criteria.Order;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Источники", description = "API для источников")
@RestController
public class BookApiController {
	private final BookRepository bookRepository;

	@Operation(summary = "Получение краткого списка источников")
	@PostMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<BookApi> getBooks(@RequestBody BookRequestApi request) {
		Specification<Book> specification = null;
		Optional<BookRequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			if (optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
			} else {
				String likeSearch = "%" + request.getSearch().getValue() + "%";
				specification = (root, query, cb) -> cb.or(cb.like(root.get("altName"), likeSearch),
					cb.like(root.get("englishName"), likeSearch),
					cb.like(root.get("name"), likeSearch));
			}
		}
		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField()))
								: cb.desc(root.get(order.getField())))
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return bookRepository.findAll(SpecificationUtil.orAll(specification), pageable).toList()
			.stream()
			.map(BookApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Получение исчтоника по английскому имения")
	@PostMapping(value = "/api/v1/books/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookApi> getBook(@PathVariable String englishName) {
		Book book = bookRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		BookApi bookApi = new BookApi(book);
		bookApi.setDescription(book.getDescription());
		return ResponseEntity.ok(bookApi);
	}

	@Operation(summary = "Источник для правки в мастерской")
	@GetMapping(value = "/api/v1/workshop/books/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	public BookEditApi getBookForEdit(@PathVariable String englishName) {
		Book book = bookRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return new BookEditApi(book);
	}

	@Operation(summary = "Правка источника в мастерской")
	@PatchMapping(value = "/api/v1/workshop/books/{source}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	public BookEditApi updateBook(@PathVariable String source, @Valid @RequestBody BookSaveApi request) {
		Book book = bookRepository.findById(source).orElseThrow(PageNotFoundException::new);
		bookRepository.findByEnglishName(request.getEnglishName().trim())
			.filter(existing -> !existing.getSource().equals(source))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with the same englishName already exists");
			});
		book.setName(request.getName().trim());
		book.setEnglishName(request.getEnglishName().trim());
		book.setAltName(trimToNull(request.getAltName()));
		book.setType(request.getType());
		book.setYear(request.getYear());
		book.setDescription(trimToNull(request.getDescription()));
		return new BookEditApi(bookRepository.saveAndFlush(book));
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
