package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class OptimisticLockConflictException extends BaseException {

    public OptimisticLockConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OptimisticLockConflictException(ErrorCode errorCode,  String message) {
        super(errorCode, message);
    }
}
