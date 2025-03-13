package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.tools.MadnessApi;
import club.dnd5.portal.dto.api.tools.RequestMadnessApi;
import club.dnd5.portal.model.Madness;
import club.dnd5.portal.model.MadnessType;
import club.dnd5.portal.repository.MadnessRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Утилиты", description = "API для генерации безумия")
@RequiredArgsConstructor
@RestController
public class MadnessApiController {
	public static final Random rnd = new Random();

	private final MadnessRepository madnessRepository;

	@Operation(summary = "Получение списка безумия")
	@GetMapping("/api/v1/tools/madness")
	public Collection<NameValueApi> getItems() {
		return Arrays
			.stream(MadnessType.values())
			.map(type -> NameValueApi.builder()
				.name(type.getCyrilicName())
				.value(type.name())
				.build())
			.collect(Collectors.toList());
	}

	@PostMapping("/api/v1/tools/madness")
	public Collection<MadnessApi> getItems(@RequestBody RequestMadnessApi request) {
		if (request.getCount() > 99) {
			return Collections.emptyList();
		}
		MadnessType madnessType;
		if (request.getType() == null)
		{
			madnessType = MadnessType.values()[rnd.nextInt(MadnessType.values().length)];
		} else {
			madnessType = MadnessType.valueOf(request.getType());
		}
		Collection<MadnessApi> madness = new ArrayList<>(request.getCount());
		List<Madness> items = madnessRepository.findByMadnessType(madnessType);
		for (int i = 0; i < request.getCount(); i++) {
			madness.add(new MadnessApi(items.get(rnd.nextInt(items.size()))));
		}
		return madness;
	}
}
