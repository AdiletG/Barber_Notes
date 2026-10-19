package kg.barbernotes.barbernotes.common.security.ratelimit;

public record RateLimitDecision(
        boolean allowed,
        long retryAfterSeconds
) {
    public static RateLimitDecision permit() {
        return new RateLimitDecision(true, 0);
    }

    public static RateLimitDecision denied(long seconds) {
        return new RateLimitDecision(false, Math.max(1, seconds));
    }
}
