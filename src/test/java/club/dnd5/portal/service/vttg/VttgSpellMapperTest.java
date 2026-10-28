package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.TimeCast;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgSpellMapperTest {
	private final VttgSpellMapper mapper = new VttgSpellMapper();

	@Test
	void burningHandsParsesConeAndSave() {
		Spell spell = spell("Огненные ладони", "Burning hands");
		spell.setLevel((byte) 1);
		spell.setSchool(MagicSchool.EVOCATION);
		spell.setDistance("На себя (15-футовый конус)");
		spell.setDuration("Мгновенная");
		spell.setSavingthrows(Collections.singletonList(AbilityType.DEXTERITY));
		spell.setDamageType(Collections.singletonList(DamageType.FAIR));
		spell.setDescription("<p>Урон <dice-roller formula=\"3к6\"/> огнём.</p>");
		spell.setSrd(true);

		Map<String, Object> data = mapper.toVttg(spell);

		assertThat(data).containsEntry("id", "burning_hands")
			.containsEntry("type", "spell")
			.containsEntry("section", "spells")
			.containsEntry("school", "evocation")
			.containsEntry("rangeUnit", "self")
			.containsEntry("targetType", "area")
			.containsEntry("deliveryType", "self")
			.containsEntry("saveType", "dexterity")
			.containsEntry("damageTypes", Collections.singletonList("fire"))
			.containsEntry("durationUnit", "instantaneous")
			.containsEntry("isSRD", true)
			.containsEntry("classKeys", Collections.singletonList("wizard"));
		assertThat(asMap(data.get("areaOfEffect"))).containsEntry("shape", "cone").containsEntry("size", 15);
		assertThat((String) data.get("description")).isEqualTo("<p>Урон 3к6 огнём.</p>");
	}

	@Test
	void rangeInFeetAndMiles() {
		Map<String, Object> feet = new LinkedHashMap<>();
		VttgSpellMapper.putRange(feet, "120 футов");
		assertThat(feet).containsEntry("range", 120).containsEntry("rangeUnit", "ft").doesNotContainKey("rangeSpecial");

		Map<String, Object> miles = new LinkedHashMap<>();
		VttgSpellMapper.putRange(miles, "1 миля");
		assertThat(miles).containsEntry("range", 1).containsEntry("rangeUnit", "mi");

		Map<String, Object> touch = new LinkedHashMap<>();
		VttgSpellMapper.putRange(touch, "Касание");
		assertThat(touch).containsEntry("range", 0).containsEntry("rangeSpecial", "Касание");
	}

	@Test
	void durationUnits() {
		assertThat(duration("Концентрация, вплоть до 1 минуты")).containsEntry("durationValue", 1)
			.containsEntry("durationUnit", "minute");
		assertThat(duration("8 часов")).containsEntry("durationValue", 8).containsEntry("durationUnit", "hour");
		assertThat(duration("10 дней")).containsEntry("durationUnit", "day");
		assertThat(duration("1 раунд")).containsEntry("durationUnit", "round");
		assertThat(duration("Пока не будет рассеяно")).containsEntry("durationUnit", "until-dispelled");
		assertThat(duration("Особая")).containsEntry("durationUnit", "special");
	}

	@Test
	void spellAttackAndReaction() {
		Spell spell = spell("Огненный снаряд", "Fire bolt");
		spell.setDistance("120 футов");
		spell.setDescription("<p>Совершите дальнобойную атаку заклинанием по цели.</p>");
		TimeCast reaction = new TimeCast();
		reaction.setNumber(1);
		reaction.setUnit(TimeUnit.REACTION);
		reaction.setCondition("совершаемая, когда вас атакуют");
		spell.setTimes(Collections.singletonList(reaction));
		spell.setAdditionalMaterialComponent("алмаз стоимостью 300 зм");
		spell.setConsumable(true);

		Map<String, Object> data = mapper.toVttg(spell);

		assertThat(data).containsEntry("deliveryType", "ranged")
			.containsEntry("castingTimeUnit", "reaction")
			.containsEntry("reactionTrigger", "совершаемая, когда вас атакуют");
		assertThat(asMap(data.get("components"))).containsEntry("material", true)
			.containsEntry("materialCost", 300)
			.containsEntry("materialConsumed", true);
	}

	private static Map<String, Object> duration(String text) {
		Map<String, Object> data = new LinkedHashMap<>();
		VttgSpellMapper.putDuration(data, text);
		return data;
	}

	private static Spell spell(String name, String englishName) {
		HeroClass wizard = new HeroClass();
		wizard.setEnglishName("Wizard");
		HeroClass homebrew = new HeroClass();
		homebrew.setEnglishName("Blood Hunter");
		Spell spell = new Spell();
		spell.setName(name);
		spell.setEnglishName(englishName);
		spell.setBook(new Book("PHB"));
		spell.setHeroClass(Arrays.asList(wizard, homebrew));
		return spell;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}
}
