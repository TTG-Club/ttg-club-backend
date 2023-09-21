package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.tools.RandomEncounterApi;
import club.dnd5.portal.dto.api.tools.RandomEncounterInputApi;
import club.dnd5.portal.dto.api.tools.RandomEncounterTableApi;
import club.dnd5.portal.dto.api.tools.RequestRandomEncounterApi;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.encounters.RandomEncounterRow;
import club.dnd5.portal.model.encounters.RandomEncounterTable;
import club.dnd5.portal.repository.datatable.RandomEncounterRepository;
import club.dnd5.portal.repository.datatable.RandomEncounterTableRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Random;

import static club.dnd5.portal.model.creature.HabitatType.*;

@Tag(name = "Утилиты", description = "API по случайным столкновениям")
@RequiredArgsConstructor
@RestController
public class RandomEncounterApiController {
	public static final Random rnd = new Random();

	private final RandomEncounterRepository repository;
	private final RandomEncounterTableRepository repoTable;

	@GetMapping("/api/v1/tools/encounters")
	public RandomEncounterInputApi getItems() {
		return new RandomEncounterInputApi(types());
	}

	@PostMapping("/api/v1/tools/encounters")
	public ResponseEntity<RandomEncounterApi> getItems(@RequestBody RequestRandomEncounterApi request) {
		if (request.getLevel() == null) {
			request.setLevel(Dice.d4.roll());
		}
		HabitatType enviroment;
		if (request.getEnvironment() == null) {
			HabitatType[] habitatTypes = types().toArray(new HabitatType[types().size()]);
			enviroment = habitatTypes[rnd.nextInt(habitatTypes.length)];
		} else {
			enviroment = valueOf(request.getEnvironment());
		}
		Optional<RandomEncounterRow> encounter = repository.findOne(Dice.d100.roll(), request.getLevel(), enviroment);
		if (encounter.isPresent()) {
			return ResponseEntity.ok(new RandomEncounterApi(encounter.get()));
		} else {
			RandomEncounterApi randomEncounter = new RandomEncounterApi();
			randomEncounter.setDescription("Нет случайных столкновений");
			return ResponseEntity.ok(randomEncounter);
		}
	}

	@PostMapping("/api/v1/tools/encounters/table")
	public ResponseEntity<RandomEncounterTableApi> getTable(@RequestBody RequestRandomEncounterApi reques) {
		Optional<RandomEncounterTable> table = repoTable.findByLevelAndType(reques.getLevel(),
				valueOf(reques.getEnvironment()));
		if (table.isPresent()) {
			RandomEncounterTableApi raTable = new RandomEncounterTableApi(table.get());
			return ResponseEntity.ok(raTable);
		}
		return ResponseEntity.notFound().build();
	}
}
