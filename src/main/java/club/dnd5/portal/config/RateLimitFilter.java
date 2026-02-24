package club.dnd5.portal.config;

import club.dnd5.portal.config.properties.RateLimitProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter
{
    private static final String ANONYMOUS_USER = "anonymousUser";

    private final RateLimitProperties properties;

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties)
    {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException, ServletException
    {
        if (shouldSkip(request))
        {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);

        BucketEntry entry = buckets.computeIfAbsent(
                clientKey,
                k -> new BucketEntry(new SimpleTokenBucket(
                        properties.getCapacity(),
                        properties.getCapacity(),
                        properties.getWindow()
                ))
        );
        entry.touch();

        ConsumeResult result = entry.bucket().tryConsume();

        if (result.isConsumed())
        {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()));
            // Можно временно включить, чтобы на проде увидеть ключ прямо в ответе:
            // response.setHeader("X-RateLimit-Key", clientKey);
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, result.getRetryAfterSeconds());

        response.setStatus(429);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");
        // response.setHeader("X-RateLimit-Key", clientKey);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write("Too many requests");
    }

    private boolean shouldSkip(HttpServletRequest request)
    {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method))
        {
            return true;
        }

        String uri = request.getRequestURI();
        // не режем health/metrics, иначе Prometheus/healthcheck быстро сожрёт лимит
        return uri != null && uri.startsWith("/actuator");
    }

    /**
     * Ключ лимита:
     * - если пользователь авторизован (не anonymous): user:<username>
     * - иначе: ip:<ip> (X-Real-IP / X-Forwarded-For / remoteAddr)
     */
    private String resolveClientKey(HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !ANONYMOUS_USER.equals(authentication.getName()))
        {
            return "user:" + authentication.getName();
        }

        String ip = resolveClientIp(request);
        return "ip:" + ip;
    }

    private String resolveClientIp(HttpServletRequest request)
    {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty())
        {
            return realIp.trim();
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty())
        {
            // Обычно XFF = "client, proxy1, proxy2"
            // При корректной настройке прокси первый адрес = клиент
            String first = forwarded.split(",")[0].trim();
            if (!first.isEmpty())
            {
                return first;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Очистка idle bucket’ов, чтобы карта не росла бесконечно.
     */
    @Scheduled(fixedDelayString = "PT5M")
    public void cleanup()
    {
        Instant cutoff = Instant.now().minus(properties.getBucketIdleTtl());
        buckets.entrySet().removeIf(e -> e.getValue().lastSeen().isBefore(cutoff));
    }

    private static final class BucketEntry
    {
        private final SimpleTokenBucket bucket;
        private final LastSeenHolder holder;

        private BucketEntry(SimpleTokenBucket bucket, LastSeenHolder holder)
        {
            this.bucket = bucket;
            this.holder = holder;
        }

        BucketEntry(SimpleTokenBucket bucket)
        {
            this(bucket, new LastSeenHolder());
        }

        void touch()
        {
            holder.touch();
        }

        Instant lastSeen()
        {
            return holder.lastSeen();
        }

        SimpleTokenBucket bucket()
        {
            return bucket;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            BucketEntry that = (BucketEntry) obj;
            return Objects.equals(this.bucket, that.bucket) &&
                    Objects.equals(this.holder, that.holder);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(bucket, holder);
        }

        @Override
        public String toString()
        {
            return "BucketEntry[" +
                    "bucket=" + bucket + ", " +
                    "holder=" + holder + ']';
        }
    }

    private static final class LastSeenHolder
    {
        private volatile Instant lastSeen = Instant.now();

        void touch()
        {
            lastSeen = Instant.now();
        }

        Instant lastSeen()
        {
            return lastSeen;
        }
    }

    /**
     * Простой token bucket с refill "greedy" (полная дозаправка раз в window).
     * Потокобезопасность: синхронизация на уровне bucket (на ключ).
     */
    private static final class SimpleTokenBucket
    {
        private final long capacity;
        private final long refillTokens;
        private final Duration window;

        private long tokens;
        private long nextRefillEpochMillis;

        SimpleTokenBucket(long capacity, long refillTokens, Duration window)
        {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.window = window;

            this.tokens = capacity;
            this.nextRefillEpochMillis = System.currentTimeMillis() + window.toMillis();
        }

        ConsumeResult tryConsume()
        {
            synchronized (this)
            {
                refillIfNeeded();

                if (tokens >= 1L)
                {
                    tokens -= 1L;
                    return new ConsumeResult(true, tokens, 0);
                }

                long now = System.currentTimeMillis();
                long waitMillis = Math.max(0L, nextRefillEpochMillis - now);
                long retryAfterSeconds = Math.max(1L, Duration.ofMillis(waitMillis).getSeconds());

                return new ConsumeResult(false, 0, retryAfterSeconds);
            }
        }

        private void refillIfNeeded()
        {
            long now = System.currentTimeMillis();

            if (now >= nextRefillEpochMillis)
            {
                tokens = Math.min(capacity, tokens + refillTokens);

                long windowMillis = window.toMillis();
                if (windowMillis <= 0L)
                {
                    // защита от некорректной конфигурации
                    nextRefillEpochMillis = now + 1000L;
                    return;
                }

                long windowsPassed = Math.max(1L, (now - nextRefillEpochMillis) / windowMillis + 1L);
                nextRefillEpochMillis = nextRefillEpochMillis + windowsPassed * windowMillis;
            }
        }

        @Override
        public String toString()
        {
            return "SimpleTokenBucket[" +
                    "capacity=" + capacity + ", " +
                    "tokens=" + tokens + ", " +
                    "window=" + window + ']';
        }
    }

    private static final class ConsumeResult
    {
        private final boolean consumed;
        private final long remainingTokens;
        private final long retryAfterSeconds;

        private ConsumeResult(boolean consumed, long remainingTokens, long retryAfterSeconds)
        {
            this.consumed = consumed;
            this.remainingTokens = remainingTokens;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        boolean isConsumed()
        {
            return consumed;
        }

        long getRemainingTokens()
        {
            return remainingTokens;
        }

        long getRetryAfterSeconds()
        {
            return retryAfterSeconds;
        }
    }
}