package kg.barbernotes.barbernotes.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import kg.barbernotes.barbernotes.common.dto.ErrorResponse;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BaseException;
import kg.barbernotes.barbernotes.common.exceptions.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;


@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> baseException(
            BaseException ex,
            HttpServletRequest request){

        log.warn("Business exception : {} at path {}", ex.getMessage(), request.getRequestURI());

        return errorService(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI());
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
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
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

        return errorService(
                ErrorCode.OPTIMISTIC_LOCK_CONFLICT,
                "Данные были изменены другим пользователем, обновите страницу",
                request.getRequestURI()) ;
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied at path {}: {}",
                request.getRequestURI(), ex.getMessage());

        return errorService(
                ErrorCode.FORBIDDEN,
                "Недостаточно прав для выполнения операции",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> httpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request
    ){

        log.warn("HttpMessageNotReadableException exception at path {}",
                request.getRequestURI());

        return errorService(
                ErrorCode.MALFORMED_REQUEST_BODY,
                "Тело запроса некорректно",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> methodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ){
        log.warn("MethodArgumentTypeMismatchException exception: {} at the path {}",
                ex.getName(), request.getRequestURI());

        return errorService(
                ErrorCode.INVALID_REQUEST_PARAMETER,
                "Некорректное значение параметра " + ex.getName(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ){
        log.warn("MissingServletRequestParameterException exception: {} at the path {}",
                ex.getParameterName(), request.getRequestURI());

        return errorService(
                ErrorCode.INVALID_REQUEST_PARAMETER,
                "Не передан обязательный параметр " + ex.getParameterName(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> httpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ){
        log.warn("HttpRequestMethodNotSupportedException exception: {} at the path {}",
                ex.getMethod(), request.getRequestURI());

        return errorService(
                ErrorCode.METHOD_NOT_ALLOWED,
                "Этот метод запроса не поддерживается для данного адреса",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request
    ){
        log.warn("NoResourceFoundException exception: {} at the path {}",
                ex.getMessage(), request.getRequestURI());

        return errorService(
                ErrorCode.ENDPOINT_NOT_FOUND,
                "Запрошенный адрес не найден",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ){
        log.warn("DataIntegrityViolationException exception: {} at the path {}",
                ex.getMostSpecificCause(), request.getRequestURI());

        return errorService(
          ErrorCode.DATA_CONFLICT,
                "Операция конфликтует с существующими данными. Возможно, такая запись уже существует",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> httpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ){
        log.warn("HttpMediaTypeNotSupportedException exception: {} at the path {}",
                ex.getMessage(), request.getRequestURI());
        return errorService(
                ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Формат содержимого запроса не поддерживается. Используйте application/json",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> rateLimitException(
            RateLimitException ex,
            HttpServletRequest request
    ){
        log.warn(
                "Rate limit exceeded: path={}, retryAfter={}s",
                request.getRequestURI(),
                ex.getRetryAfterSeconds()
        );

        ResponseEntity<ErrorResponse> errorResponse = errorService(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header(
                        HttpHeaders.RETRY_AFTER,
                        String.valueOf(ex.getRetryAfterSeconds())
                )
                .body(errorResponse.getBody());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(
            Exception ex,
            HttpServletRequest request){

        log.error("Exception at path {}", request.getRequestURI(), ex);

        return errorService(
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера. Мы уже работаем над этим.",
                request.getRequestURI());
    }




    private  ResponseEntity<ErrorResponse> errorService(ErrorCode errorCode, String message, String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getCode())
                .message((message != null && !message.isBlank()) ? message : errorCode.name())
                .path(path)
                .build();
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }
}