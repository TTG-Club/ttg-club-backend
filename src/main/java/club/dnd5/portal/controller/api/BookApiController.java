package club.dnd5.portal.controller.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Order;

import club.dnd5.portal.dto.api.wiki.RuleApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.util.SortUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.BookApi;
import club.dnd5.portal.dto.api.BookRequestApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Book", description = "The Book API")
@RestController
public class BookApiController {
	@Autowired
	private BookRepository repo;

	@Operation(summary = "Gets all books")
	@PostMapping(value = "/api/v1/books", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<BookApi> getBooks(@RequestBody BookRequestApi request) {
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Specification<Book> specification = null;
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
		Collection<Book> books;
		if (pageable == null) {
			books = repo.findAll(specification, sort);
		} else {
			books = repo.findAll(specification, pageable).toList();
		}
		return books
			.stream()
			.map(BookApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Get book by english name")
	@PostMapping(value = "/api/v1/books/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BookApi> getBook(@PathVariable String englishName) {
		Book book = repo.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		BookApi bookApi = new BookApi(book);
		bookApi.setDescription(book.getDescription());
		return ResponseEntity.ok(bookApi);
	}
}
