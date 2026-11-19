package club.dnd5.portal.service.spell;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spell.SpellRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.spells.SpellFilter;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.HealType;
import club.dnd5.portal.model.TimeUnit;
import club.dnd5.portal.model.splells.MagicSchool;
import club.dnd5.portal.model.splells.QSpell;
import club.dnd5.portal.util.querydsl.JsonbPredicates;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Условия поиска заклинаний для списка /api/v1/spells.
 * <p>
 * Типы урона, лечения, спасброски и время сотворения хранятся в jsonb и фильтруются через
 * {@code @>}; классы — связь spells_hero_class, через EXISTS-подзапрос ({@code any()}).
 * Логика условий повторяет прежние Specification один в один.
 */
@UtilityClass
public class SpellPredicateBuilder {
	private static final QSpell S = QSpell.spell;

	public static Predicate build(SpellRequestApi request) {
		BooleanBuilder where = new BooleanBuilder();
		search(request).ifPresent(where::and);
		SpellFilter filter = request.getFilter();
		if (filter == null) {
			return where;
		}
		if (!isEmpty(filter.getLevels())) {
			where.and(S.level.in(filter.getLevels().stream().map(Integer::byteValue).toList()));
		}
		if (!isEmpty(filter.getMyclass())) {
			where.and(S.heroClass.any().id.in(filter.getMyclass().stream().map(Integer::valueOf).toList()));
		}
		if (!isEmpty(filter.getSchools())) {
			where.and(S.school.in(filter.getSchools().stream().map(MagicSchool::valueOf).toList()));
		}
		if (!isEmpty(filter.getDamageTypes())) {
			where.and(JsonbPredicates.containsAny(S.damageType, filter.getDamageTypes().stream().map(DamageType::valueOf).toList()));
		}
		if (!isEmpty(filter.getHealTypes())) {
			where.and(JsonbPredicates.containsAny(S.healType, filter.getHealTypes().stream().map(HealType::valueOf).toList()));
		}
		if (!isEmpty(filter.getSavingThrows())) {
			where.and(JsonbPredicates.containsAny(S.savingthrows, filter.getSavingThrows().stream().map(AbilityType::valueOf).toList()));
		}
		yesNo(filter.getRitual(), S.ritual.eq(true), S.ritual.eq(false), where);
		yesNo(filter.getConcentration(), S.concentration.eq(true), S.concentration.eq(false), where);
		components(filter.getComponents(), where);
		if (!isEmpty(filter.getTimecast())) {
			// «1 ACTION», «10 MINUTE»: есть время сотворения с таким числом и единицей — любое из выбранных
			BooleanBuilder any = new BooleanBuilder();
			for (String timecast : filter.getTimecast()) {
				String[] parts = timecast.split("\\s");
				int number = Integer.parseInt(parts[0]);
				TimeUnit unit = TimeUnit.valueOf(parts[1]);
				any.or(JsonbPredicates.containsJson(S.times,
					"[{\"number\":" + number + ",\"unit\":\"" + unit.name() + "\"}]"));
			}
			where.and(any);
		}
		if (!isEmpty(filter.getDistance())) {
			BooleanBuilder any = new BooleanBuilder();
			filter.getDistance().forEach(distance -> any.or(S.distance.eq(distance)));
			where.and(any);
		}
		if (!isEmpty(filter.getDuration())) {
			BooleanBuilder any = new BooleanBuilder();
			filter.getDuration().forEach(duration -> any.or(S.duration.like("%" + duration + "%")));
			where.and(any);
		}
		if (!isEmpty(filter.getBooks())) {
			where.and(S.book.source.in(filter.getBooks()));
		}
		return where;
	}

	/** Поиск по названию — как SpecificationUtil.getSearch. */
	private static Optional<Predicate> search(RequestApi request) {
		String value = Optional.ofNullable(request.getSearch()).map(SearchRequest::getValue).orElse("").trim();
		if (value.isEmpty()) {
			return Optional.empty();
		}
		if (Boolean.TRUE.equals(request.getSearch().getExact())) {
			return Optional.of(S.name.eq(value.toUpperCase()));
		}
		String like = "%" + value.toUpperCase() + "%";
		return Optional.of(S.altName.upper().like(like)
			.or(S.englishName.upper().like(like))
			.or(S.name.upper().like(like)));
	}

	/** «yes» и «no» — независимые условия через И, как раньше (оба сразу дают пустой список). */
	private static void yesNo(List<String> values, Predicate yes, Predicate no, BooleanBuilder where) {
		if (isEmpty(values)) {
			return;
		}
		if (values.contains("yes")) {
			where.and(yes);
		}
		if (values.contains("no")) {
			where.and(no);
		}
	}

	/** Компоненты: 1 — вербальный, 2 — соматический, 3 — материальный, 4/5 — расходуется / нет. */
	private static void components(List<String> components, BooleanBuilder where) {
		if (isEmpty(components)) {
			return;
		}
		if (components.contains("1")) {
			where.and(S.verbalComponent.eq(true));
		}
		if (components.contains("2")) {
			where.and(S.somaticComponent.eq(true));
		}
		if (components.contains("3")) {
			where.and(S.additionalMaterialComponent.isNotNull());
		}
		if (components.contains("4")) {
			where.and(S.consumable.eq(true));
		}
		if (components.contains("5")) {
			where.and(S.consumable.eq(false));
		}
	}

	private static boolean isEmpty(Collection<?> values) {
		return values == null || values.isEmpty();
	}
}
