package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import club.dnd5.portal.model.book.Book;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgBackgroundMapperTest {
	private final VttgBackgroundMapper mapper = new VttgBackgroundMapper();

	@Test
	void acolyteHasSkillsFeatureAndNoFeatOrAbilities() {
		Background acolyte = new Background();
		acolyte.setName("Прислужник");
		acolyte.setEnglishName("Acolyte");
		acolyte.setBook(new Book("PHB"));
		acolyte.setSkills(Arrays.asList(SkillType.INSIGHT, SkillType.RELIGION));
		acolyte.setToolOwnership("Нет");
		acolyte.setLanguage("Два языка на ваш выбор");
		acolyte.setEquipmentsText("Священный символ, молитвенник");
		acolyte.setStartMoney(15);
		acolyte.setSkillName("Приют для верующих");
		acolyte.setSkillDescription("<p>Вас и ваших спутников примут в храме.</p>");
		acolyte.setDescription("<p>Вы служили в храме.</p>");
		Personalization trait = new Personalization();
		trait.setType(PersonalizationType.TRAIT);
		trait.setText("Я цитирую молитвы.");
		acolyte.setPersonalizations(Arrays.asList(trait));

		Map<String, Object> data = mapper.toVttg(acolyte);

		assertThat(data).containsEntry("type", "background")
			.containsEntry("id", "acolyte")
			.containsEntry("section", "backgrounds")
			.doesNotContainKeys("featGrant", "abilityGrant");
		assertThat(asMap(data.get("skillGrant"))).containsEntry("skills", Arrays.asList("insight", "religion"));
		assertThat(asMap(data.get("feature"))).containsEntry("name", "Приют для верующих");
		Map<String, Object> option = asMap(((List<?>) data.get("equipmentOptions")).get(0));
		assertThat(option).containsEntry("coins", 15);
		Map<String, Object> choice = asMap(((List<?>) asMap(data.get("featData")).get("choices")).get(0));
		assertThat(choice).containsEntry("type", "language").containsEntry("count", 2);
		assertThat((String) data.get("description"))
			.contains("<strong>Владение навыками:</strong> Проницательность, Религия")
			.contains("<h3>Черта характера</h3>")
			.contains("<td>Я цитирую молитвы.</td>");
	}

	@Test
	void languageChoiceCount() {
		assertThat(VttgBackgroundMapper.languageChoiceCount("Один язык на ваш выбор")).isEqualTo(1);
		assertThat(VttgBackgroundMapper.languageChoiceCount("Любой экзотический язык на выбор")).isEqualTo(1);
		assertThat(VttgBackgroundMapper.languageChoiceCount("Два языка на ваш выбор")).isEqualTo(2);
		assertThat(VttgBackgroundMapper.languageChoiceCount("Дварфийский")).isZero();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}
}
