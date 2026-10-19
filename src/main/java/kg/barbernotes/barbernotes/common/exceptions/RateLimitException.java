
package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class RateLimitException extends BaseException {

    private final long retryAfterSeconds;

    public RateLimitException(long retryAfterSeconds) {
        super(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                "Слишком много запросов. Повторите попытку позже."
        );

        if (retryAfterSeconds < 1) {
            throw new IllegalArgumentException(
                    "retryAfterSeconds должен быть не меньше 1"
            );
        }

        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
