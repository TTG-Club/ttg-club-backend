package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.DamageType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellDamageTest {
	@Test
	void shouldReturnNullWithoutRolls() {
		assertThat(SpellDamage.normalize(null)).isNull();
		assertThat(SpellDamage.normalize(new SpellDamage())).isNull();

		SpellDamage empty = new SpellDamage();
		empty.setRolls(new ArrayList<>(Arrays.asList(new SpellDamageRoll(), null)));
		assertThat(SpellDamage.normalize(empty)).isNull();
	}

	@Test
	void shouldTrimAndDeduplicate() {
		SpellDamageRoll roll = new SpellDamageRoll();
		roll.setDice(" 8d6 ");
		roll.setTypes(new ArrayList<>(Arrays.asList(DamageType.FAIR, null, DamageType.FAIR)));
		roll.setCondition("  ");
		SpellDamage damage = new SpellDamage();
		damage.setRolls(List.of(roll));
		damage.setUpcastDice(" 1d6 ");

		SpellDamage result = SpellDamage.normalize(damage);

		assertThat(result).isNotNull();
		assertThat(result.getRolls()).hasSize(1);
		assertThat(result.getRolls().get(0).getDice()).isEqualTo("8d6");
		assertThat(result.getRolls().get(0).getTypes()).containsExactly(DamageType.FAIR);
		assertThat(result.getRolls().get(0).getCondition()).isNull();
		assertThat(result.getUpcastDice()).isEqualTo("1d6");
	}

	@Test
	void shouldKeepBonusOnlyRoll() {
		SpellDamageRoll roll = new SpellDamageRoll();
		roll.setAbilityModifier(true);
		roll.setHealing(true);
		SpellDamage damage = new SpellDamage();
		damage.setRolls(List.of(roll));

		SpellDamage result = SpellDamage.normalize(damage);

		assertThat(result).isNotNull();
		assertThat(result.getRolls().get(0).isHealing()).isTrue();
		assertThat(result.getRolls().get(0).getDice()).isNull();
	}

	@Test
	void shouldWriteOnlyFields() {
		SpellDamageRoll roll = new SpellDamageRoll();
		roll.setDice("1d10");
		roll.setTypes(List.of(DamageType.FAIR));
		SpellDamage damage = new SpellDamage();
		damage.setRolls(List.of(roll));
		damage.setCantripScaling(true);

		String json = JsonMapper.builder().build().writeValueAsString(damage);

		assertThat(json).contains("\"rolls\"", "\"cantripScaling\":true", "\"dice\":\"1d10\"", "\"FAIR\"");
		SpellDamage parsed = JsonMapper.builder().build().readValue(json, SpellDamage.class);
		assertThat(parsed.getRolls().get(0).getTypes()).containsExactly(DamageType.FAIR);
		assertThat(parsed.isCantripScaling()).isTrue();
	}
}
