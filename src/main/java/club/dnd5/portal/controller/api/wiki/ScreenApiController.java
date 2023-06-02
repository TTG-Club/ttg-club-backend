package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.wiki.RuleRequestApi;
import club.dnd5.portal.dto.api.wiki.ScreenApi;
import club.dnd5.portal.dto.api.wiki.ScreenDetailApi;
import club.dnd5.portal.dto.api.wiki.ScreenRequestApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Screen", description = "The Screen API")
@RestController
public class ScreenApiController {
	@Autowired
	private ScreenRepository screenRepository;

	@PostMapping(value = "/api/v1/screens", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ScreenApi> getScreens(@RequestBody ScreenRequestApi request) {
		Specification<Screen> specification = null;

		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		} else {
			specification = Specification.where((root, query, cb) -> cb.isNull(root.get("parent")));
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Rule> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Collection<Screen> screens;
		if (pageable == null) {
			screens = screenRepository.findAll(specification, sort);
		} else {
			screens = screenRepository.findAll(specification, pageable).toList();
		}
		return screens
			.stream()
			.map(ScreenApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/screens/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScreenDetailApi> getRule(@PathVariable String englishName) {
		Screen screen = screenRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new ScreenDetailApi(screen));
	}
}
