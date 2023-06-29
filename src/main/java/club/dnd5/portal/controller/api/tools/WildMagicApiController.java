package club.dnd5.portal.controller.api.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.tools.RequestWildMagicApi;
import club.dnd5.portal.dto.api.tools.WildMagicApi;
import club.dnd5.portal.model.splells.WildMagic;
import club.dnd5.portal.repository.datatable.WildMagicRepository;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tools", description = "Генерация дикой магии")
@RestController
public class WildMagicApiController {
	public static final Random rnd = new Random();

	@Autowired
	private WildMagicRepository wildMagicRepository;

	@GetMapping("/api/v1/tools/wildmagic")
	public Collection<SourceApi> getItems() {
		return wildMagicRepository.finAllBook().stream().map(SourceApi::new).collect(Collectors.toList());
	}

	@PostMapping("/api/v1/tools/wildmagic")
	public Collection<WildMagicApi> getItems(@RequestBody RequestWildMagicApi request) {
		Collection<WildMagicApi> wildMagics = new ArrayList<WildMagicApi>(request.getCount());
		List<WildMagic> items = wildMagicRepository.findAllByBook(request.getSources());
		for (int i = 0; i < request.getCount(); i++) {
			wildMagics.add(new WildMagicApi(items.get(rnd.nextInt(items.size()))));
		}
		return wildMagics;
	}
}
