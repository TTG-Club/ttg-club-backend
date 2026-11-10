package club.dnd5.portal.model.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Раздел, к записи которого привязана картинка галереи (таблица {@code images}).
 * <p>
 * {@link #folder} — папка на img.ttg.club, где лежат картинки раздела: в неё же
 * попадают новые загрузки из мастерской.
 */
@Getter
@RequiredArgsConstructor
public enum ImageType {
	CLASS("classes"),
	SUBCLASS("classes"),
	RACE("races"),
	CREATURE("creatures"),
	GOD("gods"),
	ITEM("items"),
	MAGIC_ITEM("item_magic"),
	WEAPON("weapons"),
	ARMOR("armors");

	private final String folder;
}
