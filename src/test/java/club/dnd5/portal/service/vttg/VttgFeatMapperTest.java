package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.trait.Trait;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgFeatMapperTest {
	private final VttgFeatMapper mapper = new VttgFeatMapper();

	@Test
	void halfFeatOffersAbilityChoiceAndKeepsRequirement() {
		Trait athlete = trait("Атлет", "Athlete");
		athlete.setAbilities(Arrays.asList(AbilityType.STRENGTH, AbilityType.DEXTERITY));
		athlete.setDescription("<p>Значение Силы или Ловкости увеличивается на 1, но не выше 20.</p>");
		athlete.setRequirement("Нет");

		Map<String, Object> data = mapper.toVttg(athlete);

		assertThat(data).containsEntry("type", "feat").containsEntry("id", "athlete").containsEntry("section", "feats");
		Map<String, Object> featData = asMap(data.get("featData"));
		assertThat(featData).doesNotContainKey("prerequisite");
		Map<String, Object> choice = asMap(asMap(featData.get("abilityScoreIncrease")).get("choice"));
		assertThat(choice).containsEntry("from", Arrays.asList("strength", "dexterity"));
	}

	@Test
	void abilitiesWithoutIncreaseAreOnlyAFilter() {
		Trait grappler = trait("Борец", "Grappler");
		grappler.setAbilities(Collections.singletonList(AbilityType.STRENGTH));
		grappler.setDescription("<p>Вы совершаете броски атаки с преимуществом.</p>");
		grappler.setRequirement("Сила 13 или выше");
		grappler.setLevel(4);

		Map<String, Object> featData = asMap(mapper.toVttg(grappler).get("featData"));

		assertThat(featData).doesNotContainKey("abilityScoreIncrease");
		assertThat(asMap(featData.get("prerequisite"))).containsEntry("text", "Сила 13 или выше")
			.containsEntry("minLevel", 4);
	}

	private static Trait trait(String name, String englishName) {
		Trait trait = new Trait();
		trait.setName(name);
		trait.setEnglishName(englishName);
		trait.setBook(new Book("PHB"));
		return trait;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}
}
