package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.tools.MadnessApi;
import club.dnd5.portal.dto.api.tools.RequestMadnessApi;
import club.dnd5.portal.model.Madness;
import club.dnd5.portal.model.MadnessType;
import club.dnd5.portal.repository.MadnessRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Tools", description = "The Madness API")
@RestController
public class MadnessApiController {
	public static final Random rnd = new Random();

	private final MadnessRepository madnessRepository;

	@GetMapping("/api/v1/tools/madness")
	public Collection<NameValueApi> getItems() {
		return Arrays
			.stream(MadnessType.values())
			.map(t -> new NameValueApi(t.getCyrilicName(), t.name()))
			.collect(Collectors.toList());
	}

	@PostMapping("/api/v1/tools/madness")
	public Collection<MadnessApi> getItems(@RequestBody RequestMadnessApi reques) {
		MadnessType madnessType;
		if (reques.getType() == null)
		{
			madnessType = MadnessType.values()[rnd.nextInt(MadnessType.values().length)];
		} else {
			madnessType = MadnessType.valueOf(reques.getType());
		}
		Collection<MadnessApi> madness = new ArrayList<>(reques.getCount());
		List<Madness> items = madnessRepository.findByMadnessType(madnessType);
		for (int i = 0; i < reques.getCount(); i++) {
			madness.add(new MadnessApi(items.get(rnd.nextInt(items.size()))));
		}
		return madness;
	}
}
