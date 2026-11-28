package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.model.user.User;

/**
 * Лимиты листов персонажей: подписка их расширяет. Подписку бэкенд узнаёт по роли
 * {@code SUBSCRIBER}, которую выдаёт общий сервис авторизации (как и для лимита групп).
 */
public final class CharacterSheetLimits {
	public static final String SUBSCRIBER_ROLE = "SUBSCRIBER";

	/** Своих листов без подписки. */
	public static final int SHEETS = 10;
	/** Своих листов с подпиской. */
	public static final int SUBSCRIBER_SHEETS = 20;
	/** Чужих листов, сохранённых по ссылке, без подписки. */
	public static final int SAVED = 16;
	/** Чужих листов, сохранённых по ссылке, с подпиской. */
	public static final int SUBSCRIBER_SAVED = 40;

	/** Удалённых листов в истории без подписки. */
	public static final int HISTORY = 20;
	/** Удалённых листов в истории с подпиской. */
	public static final int SUBSCRIBER_HISTORY = 30;

	private CharacterSheetLimits() {
	}

	public static boolean isSubscriber(User user) {
		return user.getRoles() != null && user.getRoles().stream()
				.anyMatch(role -> SUBSCRIBER_ROLE.equals(role.getName()));
	}

	public static int sheets(User user) {
		return isSubscriber(user) ? SUBSCRIBER_SHEETS : SHEETS;
	}

	public static int history(User user) {
		return isSubscriber(user) ? SUBSCRIBER_HISTORY : HISTORY;
	}

	public static int saved(User user) {
		return isSubscriber(user) ? SUBSCRIBER_SAVED : SAVED;
	}
}
