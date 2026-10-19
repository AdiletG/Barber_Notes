package kg.barbernotes.barbernotes.common.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import kg.barbernotes.barbernotes.common.config.RateLimitProperties;
import kg.barbernotes.barbernotes.common.enums.RateLimitGroup;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterService {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final RateLimitProperties properties;
    private final Cache<BucketKey, Bucket> buckets;

    public RateLimiterService(RateLimitProperties properties) {
        this.properties = properties;

        this.buckets = Caffeine.newBuilder()
                .maximumSize(properties.cache().maximumSize())
                .expireAfterAccess(properties.cache().idleExpiration())
                .build();
    }

    public RateLimitDecision check(
            RateLimitGroup group,
            String key
    ) {
        Objects.requireNonNull(group, "Группа лимита не должна быть null");

        if(key == null || key.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ключ лимита не должен быть пустым"
            );
        }

        if(!properties.enabled()){
            return RateLimitDecision.permit();
        }

        BucketKey bucketKey = new BucketKey(group, key);

        Bucket bucket = buckets.get(
                bucketKey,
                this::createBucket
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        if(probe.isConsumed()) {
            return RateLimitDecision.permit();
        }

        return RateLimitDecision.denied(
                toRetryAfterSeconds(probe.getNanosToWaitForRefill())
        );

    }

    private Bucket createBucket(BucketKey key){
        RateLimitProperties.GroupProperties groupProperties =
                switch (key.group()) {
                    case IP -> properties.ip();
                    case PHONE -> properties.phone();
                };

        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(groupProperties.capacity())
                        .refillGreedy(
                                1,
                                groupProperties.refillPeriod()
                        ))
                .build();
    }

    private long toRetryAfterSeconds(long nanos) {
        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos);

        if (nanos % NANOS_PER_SECOND != 0) {
            seconds++;
        }

        return Math.max(1, seconds);
    }

    private record BucketKey(
            RateLimitGroup group,
            String key
    ) {
    }
}