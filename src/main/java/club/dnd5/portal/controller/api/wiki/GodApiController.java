package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.wiki.GodApi;
import club.dnd5.portal.dto.api.wiki.GodDetailApi;
import club.dnd5.portal.dto.api.wiki.GodRequestApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.god.Domain;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.god.Pantheon;
import club.dnd5.portal.model.god.Rank;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.GodRepository;
import club.dnd5.portal.repository.datatable.PantheonGodRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "God", description = "The God API")
@RestController
public class GodApiController {
	@Autowired
	private GodRepository godRepository;
	@Autowired
	private PantheonGodRepository pantheonRepository;

	@Autowired
	private ImageRepository imageRepository;

	@PostMapping(value = "/api/v1/gods", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<GodApi> getGods(@RequestBody GodRequestApi request) {
		Specification<God> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, God> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!request.getFilter().getAlignment().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> root.get("aligment").in(request.getFilter().getAlignment().stream().map(Alignment::valueOf).collect(Collectors.toList())));
			}
			if (!request.getFilter().getDomain().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Domain, God> join = root.join("domains", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getDomain().parallelStream().map(Domain::valueOf).collect(Collectors.toList()));
				});
			}
			if (!request.getFilter().getRank().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> root.get("rank").in(request.getFilter().getRank().stream().map(Rank::valueOf).collect(Collectors.toList())));
			}
				if (!request.getFilter().getPantheon().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Pantheon, God> pantheon = root.join("pantheon", JoinType.LEFT);
					return cb.and(pantheon.get("id").in(request.getFilter().getPantheon()));
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
		Collection<God> gods;
		if (pageable == null) {
			gods = godRepository.findAll(specification, sort);
		} else {
			gods = godRepository.findAll(specification, pageable).toList();
		}
		return gods
			.stream()
			.map(GodApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/gods/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public GodDetailApi getGod(@PathVariable String englishName) {
		God god = godRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		GodDetailApi godApi = new GodDetailApi(god);
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.GOD, god.getId());
		if (!images.isEmpty()) {
			godApi.setImages(images);
		}
		return godApi;
	}

	@PostMapping("/api/v1/filters/gods")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = godRepository.findBook(typeBook);
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
						.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
						.collect(Collectors.toList()));
				sources.add(filter);
			}
		}
		filters.setSources(sources);

		List<FilterApi> otherFilters = new ArrayList<>();

		FilterApi alignmentFilter = new FilterApi("Мировоззрение", "alignment");
		alignmentFilter.setValues(
				Alignment.getGods().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(alignmentFilter);

		FilterApi domainFilter = new FilterApi("Домены", "domain");
		domainFilter.setValues(
				Arrays.stream(Domain.values())
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(domainFilter);


		FilterApi rankFilter = new FilterApi("Ранг", "rank");
		rankFilter.setValues(
				Arrays.stream(Rank.values())
				 .map(value -> new FilterValueApi(value.getName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(rankFilter);

		FilterApi pantheonFilter = new FilterApi("Пантеоны", "pantheon");
		pantheonFilter.setValues(
				pantheonRepository.findAll().stream()
				 .map(value -> new FilterValueApi(value.getName(), value.getId()))
				 .collect(Collectors.toList()));
		otherFilters.add(pantheonFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
