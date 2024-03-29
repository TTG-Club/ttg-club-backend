package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.bookmark.BookmarkApi;
import club.dnd5.portal.model.user.Bookmark;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.repository.user.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
	public BookmarkApi updateBookmark(final User user, final BookmarkApi bookmark) {
		if (bookmark.getParentUUID() != null) {
			final Optional<Bookmark> saved = bookmarkRepository.findById(UUID.fromString(bookmark.getUuid()));
			if (saved.isPresent() && saved.get().getParent().getUuid().toString().equals(bookmark.getParentUUID())) {
				Collection<Bookmark> chields = bookmarkRepository.findByParentUuid(UUID.fromString(bookmark.getParentUUID()));
				if (saved.get().getOrder() > bookmark.getOrder()) {
					chields.forEach(b -> {
						if (b.getOrder() >= bookmark.getOrder() && b.getOrder() < saved.get().getOrder()) {
							b.incrementOrder();
						}
					});
				}
				else {
					chields.forEach(b -> {
						if (b.getOrder() <= bookmark.getOrder()) {
							b.decrimentOrder();
						}
					});
				}
				bookmarkRepository.saveAll(chields);
			} else {
				List<Bookmark> olds = saved.get().getParent().getChildren();
				olds.stream().filter( b-> b.getOrder() > saved.get().getOrder()).forEach(b -> b.decrimentOrder());
				bookmarkRepository.saveAll(olds);
				Collection<Bookmark> chields = bookmarkRepository.findByParentUuid(UUID.fromString(bookmark.getParentUUID()));
				chields.stream().filter(b -> b.getOrder() >= bookmark.getOrder()).forEach(b -> b.incrementOrder());
				bookmarkRepository.saveAll(chields);
			}
		}
		return new BookmarkApi(bookmarkRepository.saveAndFlush(getUpdatedBookmark(user, bookmark)));
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

	private Bookmark getUpdatedBookmark(User user, BookmarkApi bookmark) {
		Bookmark updatedBookmark = new Bookmark();

		updatedBookmark.setUuid(UUID.fromString(bookmark.getUuid()));
		updatedBookmark.setName(bookmark.getName());
		updatedBookmark.setOrder(bookmark.getOrder());
		updatedBookmark.setUser(user);
		updatedBookmark.setPrefix(bookmark.getPrefix());
		updatedBookmark.setUrl(bookmark.getUrl());
		if (bookmark.getParentUUID() != null) {
			Bookmark parent = bookmarkRepository.getById(UUID.fromString(bookmark.getParentUUID()));
			parent.addChild(updatedBookmark);
			updatedBookmark.setParent(parent);
		}
		return updatedBookmark;
	}
}
