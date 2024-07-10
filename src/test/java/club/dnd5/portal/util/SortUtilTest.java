package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.Order;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SortUtil class unit test")
class SortUtilTest {
	//Test data
	private static final String FIELD = "FIELD";
	private static final String ASC_UPPER_CASE = Sort.Direction.ASC.name();
	private static final String ASC_LOWER_CASE = ASC_UPPER_CASE.toLowerCase();
	private static final String FIELD_ASC = FIELD + " " + ASC_UPPER_CASE;
	private static final String FIELD_ASC_LOWER_CASE = FIELD + " " + ASC_LOWER_CASE;
	private static final String DESC_UPPER_CASE = Sort.Direction.DESC.name();
	private static final String DESC_LOWER_CASE = DESC_UPPER_CASE.toLowerCase();
	private static final String FIELD_DESC = FIELD + " " + DESC_UPPER_CASE;
	private static final String FIELD_DESC_LOWER_CASE = FIELD + " " + DESC_LOWER_CASE;
	private static final String RANDOM_VALUE = RandomStringUtils.random(5);
	private static final String FIELD_RANDOM = FIELD + " " + RANDOM_VALUE;
	private static final String ASC_DESC = Arrays.toString(new String[]{ASC_UPPER_CASE, DESC_UPPER_CASE});
	private static final String DESC_ASC = Arrays.toString(new String[]{DESC_UPPER_CASE, ASC_UPPER_CASE});
	//Possible expected results
	private static final Sort SORT_UNSORTED = Sort.unsorted();
	private static final Sort SORT_DESC_ASC = Sort.by(Sort.Order.desc(FIELD), Sort.Order.asc(FIELD));
	private static final Sort SORT_ASC_DESC = Sort.by(Sort.Order.asc(FIELD), Sort.Order.desc(FIELD));
	private static final Sort SORT_DESC = Sort.by(Sort.Order.desc(FIELD));
	private static final Sort SORT_ASC = Sort.by(Sort.Order.asc(FIELD));
	//Expected test name parts
	private static final String EMPTY = "empty";
	private static final String RANDOM = "random";
	private static final String UNSORTED = "unsorted";
	private static final String ASCENDING = "ascending";
	private static final String DESCENDING = "descending";
	private static final String ASCENDING_DESCENDING = Arrays.toString(new String[]{ASCENDING, DESCENDING});
	private static final String DESCENDING_ASCENDING = Arrays.toString(new String[]{DESCENDING, ASCENDING});


	@Test
	@DisplayName("Test getSort(RequestApi) method should throw NPE if direction is missing")
	void testGetSortRequestApiShouldThrowNpeIfDirectionIsMissing() {
		RequestApi given = new RequestApi();
		Order order = new Order();
		order.setField(FIELD);
		given.setOrders(Lists.list(order));

		assertThrows(NullPointerException.class, () -> SortUtil.getSort(given));
	}

	@ParameterizedTest(name = "With {0} direction")
	@MethodSource("directions")
	@DisplayName("Test getSort(RequestApi) method should throw IAE if field is `null`")
	void testGetSortRequestApiShouldThrowIaeIfFieldIsMissing(String direction) {
		RequestApi given = new RequestApi();
		Order order = new Order();
		order.setDirection(direction);
		given.setOrders(Lists.list(order));

		assertThrows(IllegalArgumentException.class, () -> SortUtil.getSort(given));
	}

	private static Stream<String> directions() {
		return Stream.of(ASC_UPPER_CASE, ASC_LOWER_CASE, DESC_UPPER_CASE, DESC_LOWER_CASE, RANDOM_VALUE);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("getSortRequestApiSource")
	@DisplayName("Test getSort(RequestApi) method")
	void testGetSortRequestApi(List<Order> order, Sort expected) {
		//given
		RequestApi given = new RequestApi();
		given.setOrders(order);
		//when
		Sort actual = SortUtil.getSort(given);
		//then
		assertEquals(expected, actual);
	}

	private static Stream<Arguments> getSortRequestApiSource() {
		Order asc = new Order(FIELD_ASC);
		Order desc = new Order(FIELD_DESC);
		String testNameFormat = "For RequestApi with %s order, %s sort should be returned";
		return Stream.of(
			Arguments.of(Named.of(String.format(testNameFormat, "with no data", UNSORTED), null), SORT_UNSORTED),
			Arguments.of(Named.of(String.format(testNameFormat, EMPTY, UNSORTED), Lists.emptyList()), SORT_UNSORTED),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_UPPER_CASE, ASCENDING), Lists.list(asc)), SORT_ASC),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_LOWER_CASE, ASCENDING), Lists.list(new Order(FIELD_ASC_LOWER_CASE))), SORT_ASC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_UPPER_CASE, DESCENDING), Lists.list(desc)), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_LOWER_CASE, DESCENDING), Lists.list(new Order(FIELD_DESC_LOWER_CASE))), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, RANDOM, DESCENDING), Lists.list(new Order(FIELD_RANDOM))), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_DESC, ASCENDING_DESCENDING), Lists.list(asc, desc)), SORT_ASC_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_ASC, DESCENDING_ASCENDING), Lists.list(desc, asc)), SORT_DESC_ASC)
		);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("getSortListStringSource")
	@DisplayName("Test getSort(List<String>) method")
	void testGetSortOrderList(List<String> given, Sort expected) {
		//when
		Sort actual = SortUtil.getSort(given);
		//then
		assertEquals(expected, actual);
	}

	private static Stream<Arguments> getSortListStringSource() {
		String testNameFormat = "For %s order, %s sort should be returned";
		return Stream.of(
			Arguments.of(Named.of(String.format(testNameFormat, EMPTY, UNSORTED), Lists.emptyList()), SORT_UNSORTED),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_UPPER_CASE, ASCENDING), Lists.list(FIELD_ASC)), SORT_ASC),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_LOWER_CASE, ASCENDING), Lists.list(FIELD_ASC_LOWER_CASE)), SORT_ASC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_UPPER_CASE, DESCENDING), Lists.list(FIELD_DESC)), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_LOWER_CASE, DESCENDING), Lists.list(FIELD_DESC_LOWER_CASE)), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, RANDOM, DESCENDING), Lists.list(FIELD_RANDOM)), SORT_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, ASC_DESC, ASCENDING_DESCENDING), Lists.list(FIELD_ASC, FIELD_DESC)), SORT_ASC_DESC),
			Arguments.of(Named.of(String.format(testNameFormat, DESC_ASC, DESCENDING_ASCENDING), Lists.list(FIELD_DESC, FIELD_ASC)), SORT_DESC_ASC)
		);
	}
}
