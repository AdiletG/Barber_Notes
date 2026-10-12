package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class CustomException extends BaseException {

    public CustomException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode);
    }
}
