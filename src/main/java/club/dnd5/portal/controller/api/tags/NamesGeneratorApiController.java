package club.dnd5.portal.controller.api.tags;

import club.dnd5.portal.dto.api.tags.GeneratorNameApi;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.races.RaceNickname;
import club.dnd5.portal.model.races.Sex;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Name Generator", description = "API для генерации имен")
@RequestMapping("/api/v1/")
@RestController
public class NamesGeneratorApiController {
	@GetMapping("tags/creature/type")
	public List<String> getCreatureTags() {
		return Arrays.stream(CreatureType.values()).map(CreatureType::getCyrillicName).collect(Collectors.toList());
	}

	@GetMapping("/tags/creature/name/type")
	public List<String> getCreatureTypeTags () {
		return Arrays.stream(RaceNickname.NicknameType.values()).map(RaceNickname.NicknameType::getName).collect(Collectors.toList());
	}

	@GetMapping("tags/creature/sex")
	public List<String> getSexTags() {
		return Arrays.stream(Sex.values()).map(Sex::getCyrilicName).collect(Collectors.toList());
	}

	@PostMapping("tags/generate/names")
	public List<GeneratorNameApi> getGeneratingNames(@RequestBody GeneratorNameApi generatorNameApi) {

		return new ArrayList<>();
	}
}
