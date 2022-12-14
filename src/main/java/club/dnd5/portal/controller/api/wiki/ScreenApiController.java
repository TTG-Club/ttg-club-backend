package club.dnd5.portal.controller.api.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.wiki.ScreenApi;
import club.dnd5.portal.dto.api.wiki.ScreenDetailApi;
import club.dnd5.portal.dto.api.wiki.ScreenRequestApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenDatatableRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Screen", description = "The Screen API")
@RestController
public class ScreenApiController {
	@Autowired
	private ScreenDatatableRepository screenRepository;

	@PostMapping(value = "/api/v1/screens", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ScreenApi> getScreens(@RequestBody ScreenRequestApi request) {
		Specification<Screen> specification = null;
		DataTablesInput input = new DataTablesInput();
		List<Column> columns = new ArrayList<Column>(3);
		Column column = new Column();
		column.setData("name");
		column.setName("name");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		column.setSearch(new Search("", Boolean.FALSE));
		columns.add(column);

		column = new Column();
		column.setData("englishName");
		column.setName("englishName");
		column.setSearch(new Search("", Boolean.FALSE));
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.TRUE);
		columns.add(column);

		column = new Column();
		column.setData("altName");
		column.setName("altName");
		column.setSearchable(Boolean.TRUE);
		column.setOrderable(Boolean.FALSE);

		columns.add(column);
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
		input.setColumns(columns);
		input.setLength(request.getLimit() != null ? request.getLimit() : -1);
		if (request.getPage() != null && request.getLimit() != null) {
			input.setStart(request.getPage() * request.getLimit());
		}
		if (request.getSearch() != null && request.getSearch().getValue() != null
				&& !request.getSearch().getValue().isEmpty()) {
			if (request.getSearch().getExact() != null && request.getSearch().getExact()) {
				specification = (root, query, cb) -> cb.equal(root.get("name"),
						request.getSearch().getValue().trim().toUpperCase());
			} else {
				input.getSearch().setValue(request.getSearch().getValue());
				input.getSearch().setRegex(Boolean.FALSE);
			}
		} else {
			specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.isNull(root.get("parent")));
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Rule> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		return screenRepository.findAll(input, specification, specification, ScreenApi::new).getData();
	}
	
	@PostMapping(value = "/api/v1/screens/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScreenDetailApi> getRule(@PathVariable String englishName) {
		Optional<Screen> rule = screenRepository.findByEnglishName(englishName.replace('_', ' '));
		if (!rule.isPresent()) {
			ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new ScreenDetailApi(rule.get()));
	}
}