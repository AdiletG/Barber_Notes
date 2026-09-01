package kg.barbernotes.barbernotes.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import kg.barbernotes.barbernotes.common.dto.ErrorResponse;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;


@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> baseException(
            BaseException ex,
            HttpServletRequest request){

        log.warn("Business exception : {} at path {}", ex.getMessage(), request.getRequestURI());

        return errorService(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request){

        log.warn("Validation error: {}", request.getRequestURI());

        List<ErrorResponse.ValidationError> errors = ex.getFieldErrors().stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ErrorCode.VALIDATION_FAILED.getHttpStatus().value())
                .error(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Ошибка валидации входящих данных")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return new ResponseEntity<>(response, ErrorCode.VALIDATION_FAILED.getHttpStatus());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request){

        log.warn("OptimisticLockingFailureException exception: {} at path {}", ex.getMessage(), request.getRequestURI());

        return errorService(ErrorCode.OPTIMISTIC_LOCK_CONFLICT, ex.getMessage(), request.getRequestURI()) ;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(
            Exception ex,
            HttpServletRequest request){

//        log.error("Exception: {} at path {}", ex.getMessage(), request.getRequestURI());
        log.error("Exception at path {}", request.getRequestURI(), ex);
        return errorService(ErrorCode.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера. Мы уже работаем над этим.", request.getRequestURI());
    }


    private  ResponseEntity<ErrorResponse> errorService(ErrorCode errorCode, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getCode())
                .message(message != null ? message : errorCode.name())
                .path(path)
                .build();
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }
}