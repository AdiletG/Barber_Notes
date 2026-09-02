package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class TokenReuseDetectedException extends BaseException {
    public TokenReuseDetectedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TokenReuseDetectedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
