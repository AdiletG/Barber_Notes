package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class CustomAuthenticationException extends BaseException {
    public CustomAuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CustomAuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
