package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.ArmorApi;
import club.dnd5.portal.dto.api.item.ArmorDetailApi;
import club.dnd5.portal.dto.api.item.ArmorRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Доспехи (броня)", description = "The Armor API")
@RestController
public class ArmorApiController {
	private final ArmorRepository armorRepository;

	@PostMapping(value = "/api/v1/armors", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ArmorApi> getItem(@RequestBody ArmorRequestApi request) {
		Specification<Armor> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}

		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return armorRepository.findAll(specification, pageable).toList()
			.stream()
			.map(ArmorApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/armors/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArmorDetailApi getOption(@PathVariable String englishName) {
		return new ArmorDetailApi(armorRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new));
	}
}
