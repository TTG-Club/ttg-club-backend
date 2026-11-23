package club.dnd5.portal.model.mechanics;

import club.dnd5.portal.dto.api.classes.ClassTraitSaveApi;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.effect.ActiveEffect;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JSON-контракт механики: имена полей как в core-api, пустое не выводится, незнакомые ключи
 * активного эффекта переживают чтение и запись.
 */
class MechanicsJsonTest {
	private final ObjectMapper mapper = JsonMapper.builder().build();

	@Test
	void activeEffectKeepsUnknownFieldsOnEveryLevel() {
		String json = """
			{"name":"Ярость","futureTopLevel":{"a":[1,2]},\
			"duration":{"type":"minutes","value":1,"futureDuration":"x"},\
			"changes":[{"key":"damage.melee","mode":"add","value":"2","futureChange":true,"step":{"by":1,"per":"level","futureStep":3}}],\
			"triggers":[{"event":"turnStart","then":[{"do":"heal"}]}],\
			"aura":{"radius":10,"futureAura":"y"}}""";

		ActiveEffect effect = mapper.readValue(json, ActiveEffect.class);

		assertEquals("Ярость", effect.getName());
		assertEquals("damage.melee", effect.getChanges().get(0).getKey());
		assertEquals(mapper.readTree(json), mapper.valueToTree(effect));
	}

	@Test
	void emptyFieldsAreNotWritten() {
		ClassMechanics mechanics = new ClassMechanics();
		ProficiencyGrant proficiencies = new ProficiencyGrant();
		proficiencies.setSkills(Set.of(SkillType.STEALTH));
		mechanics.setProficiencies(proficiencies);

		assertEquals(mapper.readTree("{\"proficiencies\":{\"skills\":[\"STEALTH\"]}}"), mapper.valueToTree(mechanics));
		assertEquals("{}", mapper.writeValueAsString(new ActiveEffect()));
	}

	@Test
	void fieldNamesMatchCoreApi() {
		String json = """
			{"abilityBonuses":[{"abilities":["CONSTITUTION"],"bonus":2},{"abilities":["STRENGTH","DEXTERITY"],"bonus":1,"count":2}],\
			"modifiers":{"speed":{"fly":30,"walkBonus":5},"senses":[{"type":"DARKVISION","range":60}],\
			"damage":{"resistances":["POISON"],"defenseChoices":[{"choiceKey":"element","kind":"RESISTANCE"}]},\
			"conditionImmunities":["CHARMED"],"creatureType":"FEY","initiativeBonus":2},\
			"proficiencies":{"weaponCategories":["WAR_MELE"],"armorCategories":["LIGHT"],"savingThrows":["WISDOM"],\
			"tools":[{"url":"thieves-tools","name":"Воровские инструменты"}],"languages":["ELVISH"]},\
			"choices":[{"key":"class-skills","type":"SKILL","count":2,"options":[{"value":"ARCANA","name":"Магия"}],\
			"spellFilter":{"level":0,"schools":["EVOCATION"],"castingTime":"RITUAL"},"scaling":[{"level":5,"count":3}]}],\
			"counters":[{"key":"breath","max":"1","showInTable":false,"shortRest":{"mode":"ALL"},"scaling":[{"level":6,"max":2}]}],\
			"spellList":{"groups":[{"requiredLevel":3,"count":"1","spells":[{"url":"hex","name":"Сглаз"}]}]}}""";

		RaceMechanics mechanics = mapper.readValue(json, RaceMechanics.class);

		assertEquals(List.of(AbilityType.CONSTITUTION), mechanics.getAbilityBonuses().get(0).getAbilities());
		assertEquals(SenseType.DARKVISION, mechanics.getModifiers().getSenses().get(0).getType());
		assertEquals(CastingUnit.RITUAL, mechanics.getChoices().get(0).getSpellFilter().getCastingTime());
		assertEquals(mapper.readTree(json), mapper.valueToTree(mechanics));
	}

	@Test
	void workshopRequestAcceptsMechanicsAndEffects() {
		ClassTraitSaveApi request = mapper.readValue("""
			{"name":"Боевой стиль","level":1,"description":["Текст"],\
			"mechanics":{"choices":[{"key":"style","type":"FEAT","count":1}],"spells":{"spells":[{"url":"shield","requiredLevel":3,"alwaysPrepared":true}]}},\
			"activeEffects":[{"name":"Оборона","changes":[{"key":"armorClass","mode":"add","value":"1"}],"vttgOnly":1}]}""",
			ClassTraitSaveApi.class);

		assertEquals(ChoiceType.FEAT, request.getMechanics().getChoices().get(0).getType());
		assertEquals(3, request.getMechanics().getSpells().getSpells().get(0).getRequiredLevel());
		assertNotNull(request.getActiveEffects().get(0).unknownFields().get("vttgOnly"));
		JsonNode written = mapper.valueToTree(request.getActiveEffects().get(0));
		assertEquals(1, written.get("vttgOnly").asInt());
	}
}
