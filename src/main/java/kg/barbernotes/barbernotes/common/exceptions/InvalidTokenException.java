package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidTokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
