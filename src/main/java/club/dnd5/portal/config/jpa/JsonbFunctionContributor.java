package club.dnd5.portal.config.jpa;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * Функции PostgreSQL для jsonb-колонок, доступные в HQL и QueryDSL.
 * <p>
 * {@code jsonb_contains(колонка, 'json')} — оператор {@code @>}: истина, если jsonb-колонка
 * содержит переданный JSON (для массивов — все его элементы). Работает по GIN-индексу
 * {@code jsonb_path_ops}. Второй аргумент передаётся параметром запроса.
 * <p>
 * Регистрируется через {@code META-INF/services/org.hibernate.boot.model.FunctionContributor}.
 */
public class JsonbFunctionContributor implements FunctionContributor {
	public static final String JSONB_CONTAINS = "jsonb_contains";

	@Override
	public void contributeFunctions(FunctionContributions contributions) {
		contributions.getFunctionRegistry()
			.patternDescriptorBuilder(JSONB_CONTAINS, "(?1 @> cast(?2 as jsonb))")
			.setExactArgumentCount(2)
			.setInvariantType(contributions.getTypeConfiguration()
				.getBasicTypeRegistry()
				.resolve(StandardBasicTypes.BOOLEAN))
			.register();
	}
}
