package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.bookmark.BookmarkApi;
import club.dnd5.portal.model.user.Bookmark;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.repository.user.BookmarkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookmarkServiceImplTest {
	private BookmarkRepository bookmarkRepository;
	private BookmarkServiceImpl service;
	private User user;
	private Bookmark parent;
	private List<Bookmark> children;

	@BeforeEach
	void setUp() {
		bookmarkRepository = mock(BookmarkRepository.class);
		service = new BookmarkServiceImpl(
			mock(SpellRepository.class),
			mock(BestiaryRepository.class),
			bookmarkRepository
		);
		user = new User();
		parent = bookmark(0);
		children = new ArrayList<>(Arrays.asList(bookmark(0), bookmark(1), bookmark(2), bookmark(3)));
		children.forEach(child -> child.setParent(parent));
		parent.setChildren(children);

		when(bookmarkRepository.findByParentUuid(parent.getUuid())).thenReturn(children);
		when(bookmarkRepository.findById(parent.getUuid())).thenReturn(Optional.of(parent));
		when(bookmarkRepository.saveAndFlush(any(Bookmark.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void shouldShiftOnlyItemsBetweenOldAndNewPositionWhenMovingDown() {
		Bookmark moved = children.get(1);
		when(bookmarkRepository.findById(moved.getUuid())).thenReturn(Optional.of(moved));

		BookmarkApi result = service.updateBookmark(user, updateRequest(moved, 3));

		assertEquals(Arrays.asList(0, 3, 1, 2), orders());
		assertEquals(3, result.getOrder());
	}

	@Test
	void shouldShiftOnlyItemsBetweenNewAndOldPositionWhenMovingUp() {
		Bookmark moved = children.get(3);
		when(bookmarkRepository.findById(moved.getUuid())).thenReturn(Optional.of(moved));

		BookmarkApi result = service.updateBookmark(user, updateRequest(moved, 1));

		assertEquals(Arrays.asList(0, 2, 3, 1), orders());
		assertEquals(1, result.getOrder());
	}

	private BookmarkApi updateRequest(Bookmark bookmark, int order) {
		BookmarkApi request = new BookmarkApi(bookmark);
		request.setOrder(order);
		return request;
	}

	private List<Integer> orders() {
		List<Integer> result = new ArrayList<>();
		children.forEach(bookmark -> result.add(bookmark.getOrder()));
		return result;
	}

	private Bookmark bookmark(int order) {
		Bookmark bookmark = new Bookmark();
		bookmark.setUuid(UUID.randomUUID());
		bookmark.setName("Bookmark " + order);
		bookmark.setOrder(order);
		return bookmark;
	}
}
