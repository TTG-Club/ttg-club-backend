package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.UrlApi;
import club.dnd5.portal.dto.api.item.MagicItemDetailApi;
import club.dnd5.portal.dto.api.item.MagicItemSaveApi;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.model.items.MagicThingType;
import club.dnd5.portal.model.items.Rarity;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import club.dnd5.portal.repository.datatable.MagicItemRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.service.WorkshopPreview;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Немагическая основа магического предмета: оружие и доспехи в редакторе мастерской. */
class MagicItemApiControllerTest {
	private final Weapon longsword = weapon(10, "Длинный меч", "Longsword");
	private final Weapon greatsword = weapon(11, "Двуручный меч", "Greatsword");
	private final Armor chainMail = armor(20, "Кольчуга", "Chain mail");

	private final MagicItemRepository magicItemRepository = mock(MagicItemRepository.class);
	private final WeaponRepository weaponRepository = mock(WeaponRepository.class);
	private final ArmorRepository armorRepository = mock(ArmorRepository.class);
	private final AuditService auditService = mock(AuditService.class);
	private final BookResolver bookResolver = mock(BookResolver.class);
	private final MagicItemApiController controller = new MagicItemApiController(
		magicItemRepository, mock(ImageRepository.class), bookResolver, auditService,
		mock(WorkshopPreview.class), weaponRepository, armorRepository);

	MagicItemApiControllerTest() {
		when(bookResolver.getCustomBook()).thenReturn(book());
		when(bookResolver.find(any())).thenReturn(Optional.empty());
		when(magicItemRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(magicItemRepository.saveAndFlush(any(MagicItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(weaponRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(weaponRepository.findByEnglishName("longsword")).thenReturn(Optional.of(longsword));
		when(weaponRepository.findByEnglishName("greatsword")).thenReturn(Optional.of(greatsword));
		when(armorRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(armorRepository.findByEnglishName("chain mail")).thenReturn(Optional.of(chainMail));
	}

	@Test
	void shouldLinkWeaponsAndArmorsByPageUrlInGivenOrder() {
		MagicItem item = existingItem();
		when(magicItemRepository.findById(1)).thenReturn(Optional.of(item));

		MagicItemSaveApi request = request();
		request.setWeapons(Arrays.asList("greatsword", " longsword ", "greatsword"));
		request.setArmors(Collections.singletonList("chain_mail"));

		MagicItemDetailApi detail = controller.updateItem(1, request);

		assertThat(item.getWeapons()).containsExactly(greatsword, longsword);
		assertThat(item.getArmors()).containsExactly(chainMail);
		assertThat(detail.getDetailType()).extracting(UrlApi::getUrl)
			.containsExactly("/weapons/greatsword", "/weapons/longsword", "/armors/chain_mail");
		assertThat(detail.getDetailType()).extracting(UrlApi::getName)
			.containsExactly("двуручный меч", "длинный меч", "кольчуга");
	}

	@Test
	void shouldKeepBaseItemsWhenRequestHasNoLists() {
		MagicItem item = existingItem();
		item.getWeapons().add(longsword);
		item.getArmors().add(chainMail);
		when(magicItemRepository.findById(1)).thenReturn(Optional.of(item));

		controller.updateItem(1, request());

		assertThat(item.getWeapons()).containsExactly(longsword);
		assertThat(item.getArmors()).containsExactly(chainMail);
	}

	@Test
	void shouldClearBaseItemsWithEmptyLists() {
		MagicItem item = existingItem();
		item.getWeapons().add(longsword);
		item.getArmors().add(chainMail);
		when(magicItemRepository.findById(1)).thenReturn(Optional.of(item));

		MagicItemSaveApi request = request();
		request.setWeapons(Collections.emptyList());
		request.setArmors(Collections.emptyList());

		MagicItemDetailApi detail = controller.updateItem(1, request);

		assertThat(item.getWeapons()).isEmpty();
		assertThat(item.getArmors()).isEmpty();
		assertThat(detail.getDetailType()).isNull();
	}

	@Test
	void shouldRejectUnknownWeapon() {
		MagicItem item = existingItem();
		when(magicItemRepository.findById(1)).thenReturn(Optional.of(item));

		MagicItemSaveApi request = request();
		request.setWeapons(Collections.singletonList("lightsaber"));

		assertThatThrownBy(() -> controller.updateItem(1, request))
			.isInstanceOfSatisfying(ResponseStatusException.class,
				exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
	}

	@Test
	void shouldLinkBaseItemsOnCreate() {
		MagicItemSaveApi request = request();
		request.setArmors(Collections.singletonList("chain_mail"));

		MagicItemDetailApi detail = controller.createItem(request);

		assertThat(detail.getDetailType()).extracting(UrlApi::getUrl).containsExactly("/armors/chain_mail");
	}

	@Test
	void shouldRecordBaseItemsInRevisionSnapshot() {
		MagicItem item = existingItem();
		item.getWeapons().add(longsword);
		item.getArmors().add(chainMail);
		when(magicItemRepository.findById(1)).thenReturn(Optional.of(item));

		controller.updateItem(1, request());

		ArgumentCaptor<MagicItemSaveApi> snapshot = ArgumentCaptor.forClass(MagicItemSaveApi.class);
		verify(auditService).record(eq("MAGIC_ITEM"), eq(1), eq(RevisionOperation.UPDATE), snapshot.capture());
		assertThat(snapshot.getValue().getWeapons()).containsExactly("longsword");
		assertThat(snapshot.getValue().getArmors()).containsExactly("chain_mail");
	}

	private static MagicItem existingItem() {
		MagicItem item = new MagicItem();
		item.setId(1);
		item.setName("Меч +1");
		item.setEnglishName("Sword +1");
		item.setRarity(Rarity.UNCOMMON);
		item.setType(MagicThingType.MELE_WEAPON);
		item.setCustomization(false);
		item.setBook(book());
		item.setDescription(new ArrayList<>(List.of("Бонус +1 к броскам атаки и урона.")));
		item.setCustClasses(new ArrayList<>());
		item.setWeapons(new ArrayList<>());
		item.setArmors(new ArrayList<>());
		return item;
	}

	private static MagicItemSaveApi request() {
		MagicItemSaveApi request = new MagicItemSaveApi();
		request.setName("Меч +1");
		request.setEnglishName("Sword +1");
		request.setRarity(Rarity.UNCOMMON);
		request.setType(MagicThingType.MELE_WEAPON);
		request.setDescription(List.of("Бонус +1 к броскам атаки и урона."));
		return request;
	}

	private static Book book() {
		Book book = new Book("DMG");
		book.setName("Руководство мастера");
		book.setType(TypeBook.OFFICAL);
		return book;
	}

	private static Weapon weapon(int id, String name, String englishName) {
		Weapon weapon = new Weapon();
		weapon.setId(id);
		weapon.setName(name);
		weapon.setEnglishName(englishName);
		return weapon;
	}

	private static Armor armor(int id, String name, String englishName) {
		Armor armor = new Armor();
		armor.setId(id);
		armor.setName(name);
		armor.setEnglishName(englishName);
		return armor;
	}
}
