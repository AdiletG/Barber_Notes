package kg.barbernotes.barbernotes.common.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(

        @NotEmpty
        List<String> allowedOrigins,

        @Positive
        long maxAgeSeconds

) {

    public CorsProperties(List<String> allowedOrigins, long maxAgeSeconds) {
        for (String allowedOrigin : allowedOrigins) {
            if(allowedOrigin.equals("*")){
                throw new BusinessRuleViolationException(
                        ErrorCode.INVALID_INPUT,
                        "AllowedOrigins cannot be empty"
                );
            }
        }

        this.allowedOrigins = allowedOrigins;
        this.maxAgeSeconds = maxAgeSeconds;
    }
}