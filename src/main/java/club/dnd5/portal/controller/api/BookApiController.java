package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.BookApi;
import club.dnd5.portal.dto.api.BookRequestApi;
import club.dnd5.portal.dto.api.RequestApi;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Order;
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
		return bookRepository.findAll(specification, pageable).toList()
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
}
