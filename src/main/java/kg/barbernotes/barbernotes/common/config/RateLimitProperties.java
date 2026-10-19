package kg.barbernotes.barbernotes.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,

        @NotNull @Valid
        GroupProperties ip,

        @NotNull @Valid
        GroupProperties phone,

        @NotNull @Valid
        CacheProperties cache,

        @NotNull @Valid
        BookingProperties booking


) {

    public record GroupProperties(
            @Positive int capacity,
            @NotNull Duration refillPeriod
            ){
        public GroupProperties{
            if(refillPeriod != null && (refillPeriod.isZero() || refillPeriod.isNegative())) {
                throw new IllegalArgumentException(
                        "refill-period должен быть больше нуля"
                );
            }
        }
    }

    public record CacheProperties(
            @Positive long maximumSize,
            @NotNull Duration idleExpiration
    ) {
        public CacheProperties {
            if (idleExpiration != null &&
                    (idleExpiration.isZero() || idleExpiration.isNegative())) {
                throw new IllegalArgumentException(
                        "idle-expiration должен быть больше нуля"
                );
            }
        }
    }


    public record BookingProperties(
            @Positive int maxActiveRecords,
            @Positive int maxServicesPerRecord,
            @Positive int adminActiveRecordsLimit
    ) {
    }
}