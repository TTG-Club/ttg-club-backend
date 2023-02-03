package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.dto.api.SearchResultApi;
import club.dnd5.portal.repository.SearchRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Tag(name = "Full search", description = "The search API")
@RestController
@RequestMapping("/api/v1/search")
public class FullSearchController {
	private static final Random RND = new Random();
	@Autowired
	private SearchRepository repository;

	@Operation(summary = "Gets search result", tags = "Full search")
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public SearchResultApi search(@RequestBody RequestApi request){
		return new SearchResultApi(repository.getCount(request.getSearch().getValue()), repository.search(request.getSearch().getValue(), request.getPage(), request.getLimit()));
	}

	@Operation(summary = "Gets search result", tags = "random search")
	@PostMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)
	public SearchResultApi random(@RequestBody RequestApi request){
		long count = repository.getCount("");
		List<SearchApi> apis = RND.ints(0, (int) count)
			.limit(request.getLimit())
			.mapToObj(index -> repository.findByIndex(index))
			.collect(Collectors.toList());

		return new SearchResultApi(request.getLimit(), apis);
	}
}
