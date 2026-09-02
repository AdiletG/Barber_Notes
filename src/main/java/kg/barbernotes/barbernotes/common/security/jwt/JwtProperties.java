package kg.barbernotes.barbernotes.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        TokenTtl customer,
        TokenTtl staff
) {
    public record TokenTtl(
            Integer accessTtlMinutes,
            Integer refreshTtlMinutes
    ) {}
}