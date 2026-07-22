package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.item.ItemDetailApi;
import club.dnd5.portal.dto.api.item.ItemSaveApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.repository.datatable.ItemRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemApiControllerTest {
	@Test
	void shouldReplaceCategoriesInsteadOfAppendingThem() {
		Equipment item = existingItem();
		ItemApiController controller = controllerFor(item);

		ItemSaveApi request = request();
		request.setCategories(Collections.singletonList(EquipmentType.TOOL));

		ItemDetailApi detail = controller.updateItem(1, request);

		assertThat(item.getTypes()).containsExactly(EquipmentType.TOOL);
		assertThat(detail.getCategoriesRaw()).containsExactly(EquipmentType.TOOL.name());
	}

	@Test
	void shouldKeepCurrencyWhenPriceIsSetWithoutOne() {
		Equipment item = existingItem();
		ItemApiController controller = controllerFor(item);

		ItemSaveApi request = request();
		request.setCost(25);
		request.setCurrency(null);

		ItemDetailApi detail = controller.updateItem(1, request);

		assertThat(item.getCurrency()).isEqualTo(Currency.MM);
		assertThat(detail.getPrice()).isEqualTo("25 мм");
	}

	private ItemApiController controllerFor(Equipment item) {
		ItemRepository itemRepository = mock(ItemRepository.class);
		when(itemRepository.findById(1)).thenReturn(Optional.of(item));
		when(itemRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(itemRepository.saveAndFlush(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
		return new ItemApiController(itemRepository, mock(BookResolver.class), mock(AuditService.class));
	}

	private Equipment existingItem() {
		Book book = new Book("PHB");
		book.setName("Книга игрока");
		book.setType(TypeBook.OFFICAL);

		Equipment item = new Equipment();
		item.setId(1);
		item.setBook(book);
		item.setTypes(new LinkedHashSet<>(Arrays.asList(EquipmentType.ADVENTURING_GEAR, EquipmentType.CONTAINER)));
		return item;
	}

	private ItemSaveApi request() {
		ItemSaveApi request = new ItemSaveApi();
		request.setName("Мешок");
		request.setEnglishName("Sack");
		return request;
	}
}
