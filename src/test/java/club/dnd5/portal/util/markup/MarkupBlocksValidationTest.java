package club.dnd5.portal.util.markup;

import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.dto.api.bestiary.request.LegendaryRequest;
import club.dnd5.portal.dto.api.classes.ClassSaveApi;
import club.dnd5.portal.dto.api.classes.ClassTraitSaveApi;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Описания всех разделов — блоки разметки core-api; вложенные запросы проверяются каскадно,
 * пустой блок отклоняется.
 */
class MarkupBlocksValidationTest {
	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidatorFactory() {
		validatorFactory.close();
	}

	private static List<String> paths(Object request) {
		return validator.validate(request).stream()
			.map(violation -> violation.getPropertyPath().toString())
			.toList();
	}

	@Test
	void rejectsBlankMarkupBlocksInNestedTraits() {
		ClassTraitSaveApi trait = new ClassTraitSaveApi();
		trait.setName("Второе дыхание");
		trait.setLevel((byte) 1);
		trait.setDescription(List.of(" "));

		ClassSaveApi request = new ClassSaveApi();
		request.setName("Воин");
		request.setEnglishName("Fighter");
		request.setDescription(List.of("Описание"));
		request.setDiceHp((byte) 10);
		request.setClassTraits(List.of(trait));

		assertThat(paths(request)).anyMatch(path -> path.startsWith("classTraits[0].description"));
	}

	@Test
	void rejectsBlankMarkupBlocksInCreatureActions() {
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription(List.of("Атака {@roll к20 + 4 | text:+4 | label:Атака}.", " "));

		BeastDetailRequest request = new BeastDetailRequest();
		request.setActions(List.of(action));

		assertThat(paths(request)).anyMatch(path -> path.startsWith("actions[0].description"));
	}

	@Test
	void rejectsBlankMarkupBlocksInLegendaryActions() {
		LegendaryRequest.LegendaryActionRequest action = new LegendaryRequest.LegendaryActionRequest();
		action.setName("Атака хвостом");
		action.setValue(List.of("Дракон совершает атаку хвостом.", " "));
		LegendaryRequest legendary = new LegendaryRequest();
		legendary.setList(List.of(action));

		BeastDetailRequest request = new BeastDetailRequest();
		request.setLegendary(legendary);

		assertThat(paths(request)).anyMatch(path -> path.startsWith("legendary.list[0].value"));
	}

	@Test
	void acceptsFilledMarkupBlocks() {
		LegendaryRequest.LegendaryActionRequest action = new LegendaryRequest.LegendaryActionRequest();
		action.setName("Атака хвостом");
		action.setValue(List.of("Дракон совершает {@b атаку} хвостом."));
		LegendaryRequest legendary = new LegendaryRequest();
		legendary.setList(List.of(action));
		legendary.setDescription(List.of("Дракон может совершить 3 легендарных действия."));

		BeastDetailRequest request = new BeastDetailRequest();
		request.setLegendary(legendary);

		assertThat(paths(request)).noneMatch(path -> path.startsWith("legendary"));
	}
}
