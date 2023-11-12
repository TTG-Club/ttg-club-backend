package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.tools.RequestWildMagicApi;
import club.dnd5.portal.dto.api.tools.WildMagicApi;
import club.dnd5.portal.model.splells.WildMagic;
import club.dnd5.portal.repository.datatable.WildMagicRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Tag(name = "Утилиты", description = "Api для генерация дикой магии")
@RequiredArgsConstructor
@RestController
public class WildMagicApiController {
	public static final Random rnd = new Random();

	private final WildMagicRepository wildMagicRepository;

	@GetMapping("/api/v1/tools/wildmagic")
	public Collection<SourceApi> getItems() {
		return wildMagicRepository.finAllBook().stream().map(SourceApi::new).collect(Collectors.toList());
	}

	@PostMapping("/api/v1/tools/wildmagic")
	public Collection<WildMagicApi> getItems(@RequestBody RequestWildMagicApi request) {
		Collection<WildMagicApi> wildMagics = new ArrayList<>(request.getCount());
		List<WildMagic> items = wildMagicRepository.findAllByBook(request.getSources());
		for (int i = 0; i < request.getCount(); i++) {
			wildMagics.add(new WildMagicApi(items.get(rnd.nextInt(items.size()))));
		}
		return wildMagics;
	}
}
