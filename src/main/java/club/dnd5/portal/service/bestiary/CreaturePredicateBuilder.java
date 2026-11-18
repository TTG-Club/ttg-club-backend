package club.dnd5.portal.service.bestiary;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.bestiary.BeastFilter;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.creature.ActionType;
import club.dnd5.portal.model.creature.QCreature;
import club.dnd5.portal.util.querydsl.JsonbPredicates;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Условия поиска существ по запросу списка бестиария (/api/v1/bestiary и /api/v2/bestiary).
 * <p>
 * Списки enum (среда обитания, урон, состояния) хранятся в jsonb и фильтруются через {@code @>};
 * связи (теги, действия, умения) — через EXISTS-подзапросы ({@code any()}), без JOIN и DISTINCT.
 */
@UtilityClass
public class CreaturePredicateBuilder {
	private static final QCreature C = QCreature.creature;
	/** Раса «НИП»: такие существа скрываются, если в фильтре не выбраны НИП. */
	private static final int NPC_RACE_ID = 102;

	public static Predicate build(BeastRequesApi request) {
		BooleanBuilder where = new BooleanBuilder();
		if (request == null) {
			return where;
		}
		search(request).ifPresent(where::and);

		BeastFilter filter = request.getFilter();
		if (filter == null) {
			return where;
		}
		if (isEmpty(filter.getNpc())) {
			where.and(C.raceId.ne(NPC_RACE_ID));
		}
		if (!isEmpty(filter.getChallengeRatings())) {
			where.and(C.challengeRating.in(filter.getChallengeRatings()));
		}
		if (!isEmpty(filter.getTypes())) {
			where.and(C.type.in(filter.getTypes()));
		}
		if (!isEmpty(filter.getSizes())) {
			where.and(C.size.in(filter.getSizes()));
		}
		if (!isEmpty(filter.getTags())) {
			where.and(C.races.any().id.in(filter.getTags()));
		}
		if (!isEmpty(filter.getBooks())) {
			where.and(C.book.source.in(filter.getBooks()));
		}
		moving(filter.getMoving(), where);
		if (!isEmpty(filter.getEnvironments())) {
			where.and(JsonbPredicates.containsAny(C.habitates, filter.getEnvironments()));
		}
		if (!isEmpty(filter.getVulnerabilityDamage())) {
			where.and(JsonbPredicates.containsAny(C.vulnerabilityDamages, filter.getVulnerabilityDamage()));
		}
		if (!isEmpty(filter.getResistanceDamage())) {
			where.and(JsonbPredicates.containsAny(C.resistanceDamages, filter.getResistanceDamage()));
		}
		if (!isEmpty(filter.getImmunityDamage())) {
			where.and(JsonbPredicates.containsAny(C.immunityDamages, filter.getImmunityDamage()));
		}
		if (!isEmpty(filter.getImmunityCondition())) {
			where.and(JsonbPredicates.containsAny(C.immunityStates, filter.getImmunityCondition()));
		}
		senses(filter.getSenses(), where);
		features(filter.getFeatures(), where);
		return where;
	}

	/** Поиск по названию — как SpecificationUtil.getSearch: точный по name или подстрока в трёх названиях. */
	private static Optional<Predicate> search(RequestApi request) {
		String value = Optional.ofNullable(request.getSearch()).map(SearchRequest::getValue).orElse("").trim();
		if (value.isEmpty()) {
			return Optional.empty();
		}
		if (Boolean.TRUE.equals(request.getSearch().getExact())) {
			return Optional.of(C.name.eq(value.toUpperCase()));
		}
		String like = "%" + value.toUpperCase() + "%";
		return Optional.of(C.altName.upper().like(like)
			.or(C.englishName.upper().like(like))
			.or(C.name.upper().like(like)));
	}

	private static void moving(List<String> moving, BooleanBuilder where) {
		if (isEmpty(moving)) {
			return;
		}
		if (moving.contains("fly")) {
			where.and(C.flySpeed.isNotNull());
		}
		if (moving.contains("hover")) {
			where.and(C.hover.isNotNull());
		}
		if (moving.contains("climbs")) {
			where.and(C.climbingSpeed.isNotNull());
		}
		if (moving.contains("swim")) {
			where.and(C.swimmingSpped.isNotNull());
		}
		if (moving.contains("digger")) {
			where.and(C.diggingSpeed.isNotNull());
		}
	}

	private static void senses(List<String> senses, BooleanBuilder where) {
		if (isEmpty(senses)) {
			return;
		}
		if (senses.contains("darkvision")) {
			where.and(C.darkvision.isNotNull());
		}
		if (senses.contains("trysight")) {
			where.and(C.trysight.isNotNull());
		}
		if (senses.contains("blindsight")) {
			where.and(C.blindsight.isNotNull());
		}
		if (senses.contains("tremmor")) {
			where.and(C.vibration.isNotNull());
		}
	}

	/** Умения: логово, легендарные действия или умение с подстрокой в названии — любое из выбранных. */
	private static void features(List<String> features, BooleanBuilder where) {
		if (isEmpty(features)) {
			return;
		}
		BooleanBuilder any = new BooleanBuilder();
		for (String feature : features) {
			switch (feature) {
				case "lair" -> any.or(C.lair.isNotNull());
				case "legendary" -> any.or(C.actions.any().actionType.eq(ActionType.LEGENDARY));
				default -> any.or(C.feats.any().name.like("%" + feature + "%"));
			}
		}
		where.and(any);
	}

	private static boolean isEmpty(Collection<?> values) {
		return values == null || values.isEmpty();
	}
}
