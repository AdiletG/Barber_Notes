package kg.barbernotes.barbernotes.common.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
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
        if(allowedOrigins == null || allowedOrigins.isEmpty()){
            throw new IllegalArgumentException("AllowedOrigins не может быть равен null или пустым");
        }

        for (String allowedOrigin : allowedOrigins) {
            if(allowedOrigin.equals("*")){
                throw new IllegalArgumentException(
                        "allowed-origins нельзя указывать *"
                );
            }
        }

        this.allowedOrigins = allowedOrigins;
        this.maxAgeSeconds = maxAgeSeconds;
    }
}