package club.dnd5.portal.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Короткоживущий кеш результатов валидации токена во внешнем SSO.
 *
 * <p>Решает две задачи, из-за которых «авторизация отваливалась»:
 * <ul>
 *     <li>снимает нагрузку с {@code /api/auth/me} — валидный токен проверяется не на
 *         каждый запрос, а не чаще раза в {@code freshTtlMs};</li>
 *     <li>даёт «протухшую» копию ({@code staleUntil}) для fallback, когда SSO временно
 *         недоступен (таймаут/5xx/обрыв), чтобы транзиентный сбой не логаутил пользователя.</li>
 * </ul>
 *
 * <p>Ключ — хеш токена (см. {@link AuthServiceAuthenticationFilter}), сырые JWT в долгоживущей
 * структуре не хранятся.
 */
public class TokenAuthenticationCache {
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private final long freshTtlMs;
    private final long staleTtlMs;
    private final int maxEntries;

    public TokenAuthenticationCache(long freshTtlMs, long staleTtlMs, int maxEntries) {
        this.freshTtlMs = freshTtlMs;
        this.staleTtlMs = staleTtlMs;
        this.maxEntries = maxEntries;
    }

    /** Возвращает запись, если она ещё пригодна хотя бы как stale, иначе {@code null} (с очисткой). */
    public Entry get(String key) {
        Entry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(now())) {
            cache.remove(key, entry);
            return null;
        }
        return entry;
    }

    public void put(String key, ExternalAuthUser user) {
        long now = now();
        if (cache.size() >= maxEntries) {
            evictExpired(now);
        }
        cache.put(key, new Entry(user, now + freshTtlMs, now + staleTtlMs));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    private void evictExpired(long now) {
        cache.values().removeIf(entry -> entry.isExpired(now));
    }

    private long now() {
        return System.currentTimeMillis();
    }

    public static final class Entry {
        private final ExternalAuthUser user;
        private final long freshUntil;
        private final long staleUntil;

        Entry(ExternalAuthUser user, long freshUntil, long staleUntil) {
            this.user = user;
            this.freshUntil = freshUntil;
            this.staleUntil = staleUntil;
        }

        public ExternalAuthUser getUser() {
            return user;
        }

        /** Свежая запись — можно авторизовать без обращения к SSO. */
        public boolean isFresh() {
            return System.currentTimeMillis() < freshUntil;
        }

        /** Пригодна как fallback при недоступности SSO. */
        public boolean isUsableStale(long now) {
            return now < staleUntil;
        }

        boolean isExpired(long now) {
            return now >= staleUntil;
        }
    }
}
