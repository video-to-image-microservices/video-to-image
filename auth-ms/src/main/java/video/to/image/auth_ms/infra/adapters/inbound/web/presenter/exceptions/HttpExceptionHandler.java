package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ConflictException;
import video.to.image.auth_ms.core.domain.exceptions.ForbiddenException;
import video.to.image.auth_ms.core.domain.exceptions.NotFoundException;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HttpExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<HttpExceptionMessage> handleNotFound(NotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new HttpExceptionMessage(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<HttpExceptionMessage> handleConflict(ConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new HttpExceptionMessage(HttpStatus.CONFLICT.value(), e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<HttpExceptionMessage> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new HttpExceptionMessage(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<HttpExceptionMessage> handleForbidden(ForbiddenException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new HttpExceptionMessage(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpExceptionMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new HttpExceptionMessage(
                        HttpStatus.BAD_REQUEST.value(),
                        ConstMessagesEnum.VALIDATION_FAILED.getMessagem(),
                        errors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpExceptionMessage> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new HttpExceptionMessage(
                        HttpStatus.BAD_REQUEST.value(),
                        ConstMessagesEnum.VALIDATION_FAILED.getMessagem(),
                        errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HttpExceptionMessage> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new HttpExceptionMessage(
                        HttpStatus.BAD_REQUEST.value(),
                        ConstMessagesEnum.INVALID_REQUEST.getMessagem()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpExceptionMessage> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new HttpExceptionMessage(
                        HttpStatus.BAD_REQUEST.value(),
                        ConstMessagesEnum.INVALID_REQUEST.getMessagem()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<HttpExceptionMessage> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new HttpExceptionMessage(
                        HttpStatus.CONFLICT.value(),
                        ConstMessagesEnum.DATA_INTEGRITY_VIOLATION.getMessagem()
                ));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<HttpExceptionMessage> handleDataAccess(DataAccessException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HttpExceptionMessage(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ConstMessagesEnum.INTERNAL_ERROR.getMessagem()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpExceptionMessage> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new HttpExceptionMessage(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ConstMessagesEnum.INTERNAL_ERROR.getMessagem()
                ));
    }
}
