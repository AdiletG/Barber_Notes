package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class TelegramSendException extends BaseException {

    public TelegramSendException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TelegramSendException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
