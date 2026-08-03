package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class BusinessRuleViolationException extends BaseException {

    public BusinessRuleViolationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleViolationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
