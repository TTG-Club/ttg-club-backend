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
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, result.getRetryAfterSeconds());

        response.setStatus(429);
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write("Too many requests");
    }

    /**
     * Ключ лимита:
     * - если пользователь авторизован: user:<username>
     * - иначе: ip:<ip> (с учётом X-Forwarded-For)
     */
    private String resolveClientKey(HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null)
        {
            return "user:" + authentication.getName();
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty())
        {
            String ip = forwarded.split(",")[0].trim();
            return "ip:" + ip;
        }

        return "ip:" + request.getRemoteAddr();
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
            long retryAfterSeconds = 0;

            synchronized (this)
            {
                refillIfNeeded();

                if (tokens >= (long) 1)
                {
                    tokens -= 1;
                    return new ConsumeResult(true, tokens, 0);
                }

                long now = System.currentTimeMillis();
                long waitMillis = Math.max(0, nextRefillEpochMillis - now);
                retryAfterSeconds = Math.max(1, Duration.ofMillis(waitMillis).getSeconds());

                return new ConsumeResult(false, 0, retryAfterSeconds);
            }
        }

        private void refillIfNeeded()
        {
            long now = System.currentTimeMillis();

            if (now >= nextRefillEpochMillis)
            {
                // greedy refill: раз в window добавляем refillTokens, не превышая capacity
                tokens = Math.min(capacity, tokens + refillTokens);

                long windowsPassed = Math.max(1, (now - nextRefillEpochMillis) / window.toMillis() + 1);
                nextRefillEpochMillis = nextRefillEpochMillis + windowsPassed * window.toMillis();
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