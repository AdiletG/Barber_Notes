package kg.barbernotes.barbernotes.common.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.failed-attempt")
public record FailedAttemptProperties(
        @Positive
        int limit,

        @NotNull
        Duration lockTime
) {}