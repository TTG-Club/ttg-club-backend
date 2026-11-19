package club.dnd5.portal.util.querydsl;

import club.dnd5.portal.config.jpa.JsonbFunctionContributor;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Условия QueryDSL для jsonb-колонок с массивом имён enum (например, {@code ["FIRE","COLD"]}).
 * <p>
 * Каждое значение проверяется оператором {@code @>} (функция {@link JsonbFunctionContributor#JSONB_CONTAINS}),
 * который использует GIN-индекс {@code jsonb_path_ops}. JSON передаётся параметром запроса.
 */
@UtilityClass
public class JsonbPredicates {

	/** Колонка содержит хотя бы одно из значений (как прежний JOIN ... IN (...)). */
	public static Predicate containsAny(Expression<?> jsonbArray, Collection<? extends Enum<?>> values) {
		BooleanBuilder any = new BooleanBuilder();
		for (Enum<?> value : values) {
			any.or(contains(jsonbArray, value));
		}
		return any;
	}

	/** В jsonb-массиве объектов есть элемент, у которого поле {@code field} подходит под LIKE-шаблон. */
	public static Predicate anyFieldLike(Expression<?> jsonbArray, String field, String likePattern) {
		return Expressions.booleanTemplate(JsonbFunctionContributor.JSONB_ANY_FIELD_LIKE + "({0}, {1}, {2})",
			jsonbArray, Expressions.constant(field), Expressions.constant(likePattern));
	}

	/** Колонка содержит значение. */
	public static Predicate contains(Expression<?> jsonbArray, Enum<?> value) {
		return Expressions.booleanTemplate(JsonbFunctionContributor.JSONB_CONTAINS + "({0}, {1})",
			jsonbArray, Expressions.constant("[\"" + value.name() + "\"]"));
	}
}
