package kg.barbernotes.barbernotes.common.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.availableSlot")
public record BookingConfig(
        @Positive
        int slotStepMinutes,

        @Positive
        int breakMinutes
) {


}