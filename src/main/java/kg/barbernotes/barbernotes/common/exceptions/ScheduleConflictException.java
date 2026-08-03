package kg.barbernotes.barbernotes.common.exceptions;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;

public class ScheduleConflictException extends BusinessRuleViolationException {
  public ScheduleConflictException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ScheduleConflictException(ErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
