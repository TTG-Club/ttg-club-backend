package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.PaginatedResponseApi;
import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.repository.SearchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Tag(name = "Поиск по сайту", description = "API поиска по сайту")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/search")
public class FullSearchController {
	private static final Random RND = new Random();

	private final SearchRepository repository;

	@Operation(summary = "Результаты поиска")
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public PaginatedResponseApi<SearchApi> search(@RequestBody RequestApi request) {
		return new PaginatedResponseApi<>(
			repository.search(request.getSearch().getValue(), request.getPage(), request.getSize()),
			repository.getCount(request.getSearch().getValue()),
			request.getPage(),
			request.getSize()
		);
	}

	@Operation(summary = "Результаты случайного поиска")
	@PostMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)
	public PaginatedResponseApi<SearchApi> random(@RequestBody RequestApi request) {
		long count = repository.getCount("");
		List<SearchApi> apis = RND.ints(0, (int) count)
			.limit(request.getSize())
			.mapToObj(repository::findByIndex)
			.collect(Collectors.toList());

		return new PaginatedResponseApi<>(
			apis,
			count,
			request.getPage(),
			request.getSize()
		);
	}
}
