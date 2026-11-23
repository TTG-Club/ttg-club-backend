package club.dnd5.portal.model.classes;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureScalingTest {
	/**
	 * Строка роста.
	 */
	private static FeatureScaling row(int level, String name, String additional) {
		FeatureScaling row = new FeatureScaling();
		row.setLevel(level);
		row.setName(name);
		row.setAdditional(additional);
		return row;
	}

	@Test
	void shouldDropInvalidRowsAndSortByLevel() {
		FeatureScaling hidden = row(12, " ", " (3) ");
		hidden.setHideInSubclasses(true);
		hidden.setDescription(new ArrayList<>(Arrays.asList(" Текст ", "  ")));

		List<FeatureScaling> result = FeatureScaling.normalize(
			new ArrayList<>(Arrays.asList(hidden, null, row(0, "Нет", null), row(8, "Свое", null))), true);

		assertThat(result).extracting(FeatureScaling::getLevel).containsExactly(8, 12);
		assertThat(result.get(1).getName()).isNull();
		assertThat(result.get(1).getAdditional()).isEqualTo("(3)");
		assertThat(result.get(1).getDescription()).containsExactly("Текст");
		assertThat(result.get(1).isHideInSubclasses()).isTrue();
	}

	@Test
	void shouldNotHideArchetypeRows() {
		FeatureScaling hidden = row(6, null, null);
		hidden.setHideInSubclasses(true);

		assertThat(FeatureScaling.normalize(List.of(hidden), false).get(0).isHideInSubclasses()).isFalse();
		assertThat(FeatureScaling.normalize(null, true)).isEmpty();
	}

	@Test
	void shouldUseTraitNameWhenOwnIsEmpty() {
		assertThat(row(8, null, null).displayName("Увеличение характеристик"))
			.isEqualTo("Увеличение характеристик");
		assertThat(row(11, " Дополнительная атака (2) ", null).displayName("Дополнительная атака"))
			.isEqualTo("Дополнительная атака (2)");
	}

	@Test
	void shouldMatchMigrationJsonShape() {
		String json = "{\"level\":8,\"name\":null,\"additional\":\"(2)\",\"description\":[],\"hideInSubclasses\":false}";

		FeatureScaling parsed = JsonMapper.builder().build().readValue(json, FeatureScaling.class);

		assertThat(parsed.getLevel()).isEqualTo(8);
		assertThat(parsed.getAdditional()).isEqualTo("(2)");
		assertThat(JsonMapper.builder().build().writeValueAsString(parsed))
			.contains("\"level\":8", "\"hideInSubclasses\":false", "\"description\":[]");
	}
}
