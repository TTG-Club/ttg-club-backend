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
	/**
	 * {@code jsonb_any_field_like(колонка, 'поле', 'шаблон')} — есть элемент jsonb-массива объектов,
	 * у которого текстовое поле подходит под LIKE-шаблон. Сравнение — в collation {@code ci}
	 * (без регистра и диакритики), как у строковых колонок.
	 */
	public static final String JSONB_ANY_FIELD_LIKE = "jsonb_any_field_like";

	@Override
	public void contributeFunctions(FunctionContributions contributions) {
		var bool = contributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN);
		contributions.getFunctionRegistry()
			.patternDescriptorBuilder(JSONB_CONTAINS, "(?1 @> cast(?2 as jsonb))")
			.setExactArgumentCount(2)
			.setInvariantType(bool)
			.register();
		contributions.getFunctionRegistry()
			.patternDescriptorBuilder(JSONB_ANY_FIELD_LIKE,
				"exists (select 1 from jsonb_array_elements(?1) as je(v) where (je.v ->> cast(?2 as text)) collate ci like ?3)")
			.setExactArgumentCount(3)
			.setInvariantType(bool)
			.register();
	}
}
