package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.bookmark.BookmarkApi;
import club.dnd5.portal.model.user.Bookmark;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.repository.user.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookmarkServiceImpl implements BookmarkService {
	private final SpellRepository spellRepository;
	private final BestiaryRepository bestiaryRepository;
	private final BookmarkRepository bookmarkRepository;

	@Override
	public Collection<BookmarkApi> getBookmarks(User user) {
		return bookmarkRepository.findByUser(user)
			.stream()
			.map(BookmarkApi::new)
			.collect(Collectors.toList());
	}

	@Override
	public BookmarkApi addBookmark(User user, BookmarkApi bookmark) {
		Bookmark entityBookmark = new Bookmark();

		entityBookmark.setUser(user);
		entityBookmark.setUuid(getNewUUID());
		entityBookmark.setName(bookmark.getName());
		if (Objects.nonNull(bookmark.getUrl()) && bookmark.getUrl().contains("/spells/")) {
			String englishName = bookmark.getUrl().replace("/spells/", "").replace('_', ' ');
			spellRepository.findByEnglishName(englishName)
				.ifPresent(spell -> entityBookmark.setPrefix(String.valueOf(spell.getLevel())));
		} else if (Objects.nonNull(bookmark.getUrl()) && bookmark.getUrl().contains("/bestiary/")) {
			String englishName = bookmark.getUrl().replace("/bestiary/", "").replace('_', ' ');
			bestiaryRepository.findByEnglishName(englishName)
				.ifPresent(creature -> entityBookmark.setPrefix(String.valueOf(creature.getChallengeRating())));
		}

		if (Objects.nonNull(bookmark.getOrder())) {
			entityBookmark.setOrder(bookmark.getOrder());
		} else if (bookmark.getParentUUID() != null) {
			entityBookmark.setOrder(
				bookmarkRepository
					.findByParentUuid(UUID.fromString(bookmark.getParentUUID()))
					.size()
			);
		}

		if (bookmark.getParentUUID() != null) {
			Bookmark parent = bookmarkRepository.findById(UUID.fromString(bookmark.getParentUUID()))
				.orElseThrow(() -> new RuntimeException("Bookmark's group not found"));

			entityBookmark.setParent(parent);

			if (bookmark.getUrl() != null) {
				entityBookmark.setUrl(bookmark.getUrl());
			}
		}
		return new BookmarkApi(bookmarkRepository.saveAndFlush(entityBookmark));
	}

	@Override
	@Transactional
	public BookmarkApi updateBookmark(final User user, final BookmarkApi bookmark) {
		Bookmark saved = bookmarkRepository.findById(UUID.fromString(bookmark.getUuid()))
			.orElseThrow(() -> new RuntimeException("Bookmark not found"));

		if (bookmark.getParentUUID() != null && bookmark.getOrder() != null) {
			UUID targetParentUuid = UUID.fromString(bookmark.getParentUUID());
			Bookmark targetParent = bookmarkRepository.findById(targetParentUuid)
				.orElseThrow(() -> new RuntimeException("Bookmark's group not found"));

			if (saved.getParent() != null && !saved.getParent().getUuid().equals(targetParentUuid)) {
				normalizeWithout(saved.getParent().getUuid(), saved);
			}

			reorder(targetParentUuid, saved, bookmark.getOrder());
			saved.setParent(targetParent);
		}

		saved.setName(bookmark.getName());
		if (bookmark.getParentUUID() == null) {
			saved.setOrder(bookmark.getOrder());
		}
		saved.setUser(user);
		saved.setPrefix(bookmark.getPrefix());
		saved.setUrl(bookmark.getUrl());

		return new BookmarkApi(bookmarkRepository.saveAndFlush(saved));
	}

	private void reorder(UUID parentUuid, Bookmark moved, int targetOrder) {
		List<Bookmark> siblings = orderedChildren(parentUuid);
		siblings.removeIf(item -> item.getUuid().equals(moved.getUuid()));
		siblings.add(Math.max(0, Math.min(targetOrder, siblings.size())), moved);
		normalize(siblings);
	}

	private void normalizeWithout(UUID parentUuid, Bookmark removed) {
		List<Bookmark> siblings = orderedChildren(parentUuid);
		siblings.removeIf(item -> item.getUuid().equals(removed.getUuid()));
		normalize(siblings);
	}

	private List<Bookmark> orderedChildren(UUID parentUuid) {
		return bookmarkRepository.findByParentUuid(parentUuid).stream()
			.sorted(Comparator
				.comparing(Bookmark::getOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(Bookmark::getUuid))
			.collect(Collectors.toList());
	}

	private void normalize(List<Bookmark> bookmarks) {
		for (int order = 0; order < bookmarks.size(); order++) {
			bookmarks.get(order).setOrder(order);
		}
		bookmarkRepository.saveAll(bookmarks);
	}

	@Override
	public void deleteBookmark(String uuid) {
		final Optional<Bookmark> saved = bookmarkRepository.findById(UUID.fromString(uuid));
		if (saved.isPresent()) {
			Bookmark parent = saved.get().getParent();
			if (parent != null) {
				Collection<Bookmark> chields = saved.get().getParent().getChildren();
				chields.stream().filter(b -> b.getOrder() > saved.get().getOrder()).forEach(b -> b.decrimentOrder());
				bookmarkRepository.saveAll(chields);
			}
			bookmarkRepository.deleteById(UUID.fromString(uuid));
		}
	}

	@Override
	public Collection<BookmarkApi> getRootBookmarks(User user) {
		return bookmarkRepository.findByUserAndParentIsNull(user)
				.stream()
				.map(BookmarkApi::new)
				.collect(Collectors.toList());
	}

	private UUID getNewUUID() {
		UUID uuid = UUID.randomUUID();
		if (bookmarkRepository.existsById(uuid)) {
			uuid = getNewUUID();
		}
		return uuid;
	}

}
