package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.item.ItemDetailApi;
import club.dnd5.portal.dto.api.item.ItemSaveApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.repository.datatable.ItemRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

	@Test
	void shouldRecordStateBeforeUpdate() {
		Equipment item = existingItem();
		item.setName("Old name");
		item.setEnglishName("Old name");
		item.setCost(10);
		item.setCurrency(Currency.GM);
		AuditService auditService = mock(AuditService.class);
		ItemApiController controller = controllerFor(item, auditService);

		ItemSaveApi request = request();
		request.setName("New name");
		request.setEnglishName("New name");
		request.setCost(25);
		request.setCurrency(Currency.MM);
		request.setCategories(Collections.singletonList(EquipmentType.TOOL));

		controller.updateItem(1, request);

		ArgumentCaptor<ItemSaveApi> snapshot = ArgumentCaptor.forClass(ItemSaveApi.class);
		verify(auditService).record(eq("ITEM"), eq(1), eq(RevisionOperation.UPDATE), snapshot.capture());
		assertThat(snapshot.getValue().getName()).isEqualTo("Old name");
		assertThat(snapshot.getValue().getCost()).isEqualTo(10);
		assertThat(snapshot.getValue().getCurrency()).isEqualTo(Currency.GM);
		assertThat(snapshot.getValue().getCategories())
			.containsExactly(EquipmentType.ADVENTURING_GEAR, EquipmentType.CONTAINER);
		assertThat(item.getName()).isEqualTo("New name");
	}

	private ItemApiController controllerFor(Equipment item) {
		return controllerFor(item, mock(AuditService.class));
	}

	private ItemApiController controllerFor(Equipment item, AuditService auditService) {
		ItemRepository itemRepository = mock(ItemRepository.class);
		when(itemRepository.findById(1)).thenReturn(Optional.of(item));
		when(itemRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(itemRepository.saveAndFlush(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
		return new ItemApiController(itemRepository, mock(BookResolver.class), auditService);
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
