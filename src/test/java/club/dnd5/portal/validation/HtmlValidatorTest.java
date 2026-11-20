package club.dnd5.portal.validation;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.bestiary.LegendaryApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.dto.api.bestiary.request.DescriptionRequest;
import club.dnd5.portal.dto.api.item.MagicItemSaveApi;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ValidHtml} остался у полей, которые ещё хранят HTML (особое свойство и уточнения магического
 * предмета, значение легендарного действия из старых клиентов); описания всех разделов — разметка
 * core-api, они проверяются как непустые блоки.
 */
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

	private static MagicItemSaveApi item(String special) {
		MagicItemSaveApi item = new MagicItemSaveApi();
		item.setSpecial(special);
		return item;
	}

	private static List<String> paths(Object request) {
		return validator.validate(request).stream()
			.map(violation -> violation.getPropertyPath().toString())
			.toList();
	}

	@Test
	void acceptsValidHtmlFragment() {
		assertThat(paths(item("<p class=\"lead\">Текст <strong>описания</strong><br></p>")))
			.doesNotContain("special");
	}

	@Test
	void rejectsMismatchedAndUnclosedTags() {
		Set<ConstraintViolation<MagicItemSaveApi>> violations = validator.validate(item("<p><strong>Некорректное описание</p>"));

		assertThat(violations)
			.anySatisfy(violation -> {
				assertThat(violation.getPropertyPath().toString()).isEqualTo("special");
				assertThat(violation.getMessage()).isEqualTo("Некорректный HTML");
			});
	}

	@Test
	void acceptsSelfClosingCustomElements() {
		assertThat(paths(item("<p><em>Попадание:</em>&nbsp;15&nbsp;(<dice-roller formula=\"3к6 + 5\"/>) урона излучением.</p>")))
			.doesNotContain("special");
	}

	@Test
	void acceptsRealOldFrontendMarkup() {
		assertThat(paths(item("<p>Колдовство (<dice-roller formula=\"к20 + 9\">+9</dice-roller>):</p><div class=\"spell-list\">"
			+ "<p>Заговоры: <em><detail-tooltip type=\"spell\"><a href=\"/spells/Guidance\">указание</a></detail-tooltip>,&nbsp;</em></p></div>")))
			.doesNotContain("special");
	}

	@Test
	void stillRejectsUnclosedCustomElement() {
		assertThat(paths(item("<p><dice-roller formula=\"1d6\">1к6</p>"))).contains("special");
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
		request.setClassTraits(Collections.singletonList(trait));

		assertThat(paths(request)).anyMatch(path -> path.startsWith("classTraits[0].description"));
	}

	@Test
	void rejectsBlankMarkupBlocksInCreatureActions() {
		DescriptionRequest action = new DescriptionRequest();
		action.setDescription(List.of("Атака {@roll к20 + 4 | text:+4 | label:Атака}.", " "));

		BeastDetailRequest request = new BeastDetailRequest();
		request.setActions(Collections.singletonList(action));

		assertThat(paths(request)).anyMatch(path -> path.startsWith("actions[0].description"));
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

		assertThat(paths(request)).contains("legendary.list[0].value");
	}
}
