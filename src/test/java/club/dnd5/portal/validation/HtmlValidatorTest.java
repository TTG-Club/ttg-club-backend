package club.dnd5.portal.validation;

import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.dto.api.bestiary.LegendaryApi;
import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.classes.ClassSaveApi;
import club.dnd5.portal.dto.api.classes.ClassTraitSaveApi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlValidatorTest {
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

	@Test
	void acceptsValidHtmlFragment() {
		ClassSaveApi request = new ClassSaveApi();
		request.setName("Воин");
		request.setEnglishName("Fighter");
		request.setDescription("<p class=\"lead\">Текст <strong>описания</strong><br></p>");
		request.setDiceHp((byte) 10);

		assertThat(validator.validate(request)).isEmpty();
	}

	@Test
	void rejectsMismatchedAndUnclosedTags() {
		ClassSaveApi request = new ClassSaveApi();
		request.setName("Воин");
		request.setEnglishName("Fighter");
		request.setDescription("<p><strong>Некорректное описание</p>");
		request.setDiceHp((byte) 10);

		Set<ConstraintViolation<ClassSaveApi>> violations = validator.validate(request);

		assertThat(violations)
			.anySatisfy(violation -> {
				assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
				assertThat(violation.getMessage()).isEqualTo("Некорректный HTML");
			});
	}

	@Test
	void acceptsSelfClosingCustomElements() {
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription("<p><em>Попадание:</em>&nbsp;15&nbsp;(<dice-roller formula=\"3к6 + 5\"/>) урона излучением.</p>");

		BeastDetailRequest request = new BeastDetailRequest();
		request.setActions(Collections.singletonList(action));

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.doesNotContain("actions[0].description");
	}

	@Test
	void acceptsRealCreatureMarkup() {
		DescriptionRequest feat = new DescriptionRequest();
		feat.setDescription("<p>Если свет перекрывается с областью <detail-tooltip type=\"spell\"><a href=\"/spells/Darkness\" >тьмы</a></detail-tooltip>.</p>");
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription("<p>Урон огнем 14&nbsp;(<dice-roller formula=\"4d6\">4к6</dice-roller>), спасбросок <span class=\"saving_throw\">Ловкости</span>.</p>");
		DescriptionRequest spells = new DescriptionRequest();
		spells.setDescription("<p>Колдовство (<dice-roller formula=\"к20 + 9\">+9</dice-roller>):</p><div class=\"spell-list\"><p>Заговоры: <em><detail-tooltip type=\"spell\"><a href=\"/spells/Guidance\">указание</a></detail-tooltip>,&nbsp;</em></p></div>");

		BeastDetailRequest request = new BeastDetailRequest();
		request.setFeats(java.util.Arrays.asList(feat, spells));
		request.setActions(Collections.singletonList(action));

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.noneMatch(path -> path.endsWith("description"));
	}

	@Test
	void stillRejectsUnclosedCustomElement() {
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription("<p><dice-roller formula=\"1d6\">1к6</p>");

		BeastDetailRequest request = new BeastDetailRequest();
		request.setActions(Collections.singletonList(action));

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("actions[0].description");
	}

	@Test
	void validatesNestedEditorBlocks() {
		ClassTraitSaveApi trait = new ClassTraitSaveApi();
		trait.setName("Второе дыхание");
		trait.setLevel((byte) 1);
		trait.setDescription("<div><span>Некорректный блок</div>");

		ClassSaveApi request = new ClassSaveApi();
		request.setName("Воин");
		request.setEnglishName("Fighter");
		request.setDescription("<p>Описание</p>");
		request.setDiceHp((byte) 10);
		request.setClassTraits(Collections.singletonList(trait));

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("classTraits[0].description");
	}

	@Test
	void validatesNestedCreatureActions() {
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription("<strong>Незакрытое описание");

		BeastDetailRequest request = new BeastDetailRequest();
		request.setActions(Collections.singletonList(action));

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("actions[0].description");
	}

	@Test
	void validatesLegendaryActionStoredInGenericValueField() {
		NameValueApi action = NameValueApi.builder()
			.name("Атака хвостом")
			.value("<p><strong>Некорректное действие</p>")
			.build();
		LegendaryApi legendary = new LegendaryApi();
		legendary.setList(Collections.singletonList(action));

		BeastDetailRequest request = new BeastDetailRequest();
		request.setLegendary(legendary);

		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.contains("legendary.list[0].value");
	}
}
