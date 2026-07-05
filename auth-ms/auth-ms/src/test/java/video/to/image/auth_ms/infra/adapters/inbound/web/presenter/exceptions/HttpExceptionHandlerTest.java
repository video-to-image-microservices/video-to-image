package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ConflictException;
import video.to.image.auth_ms.core.domain.exceptions.ForbiddenException;
import video.to.image.auth_ms.core.domain.exceptions.NotFoundException;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;
import video.to.image.auth_ms.infra.adapters.inbound.web.controller.AuthController;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login.AuthLoginRequestDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpExceptionHandlerTest {

    private HttpExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HttpExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturn404() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleNotFound(
                new NotFoundException(ConstMessagesEnum.NOT_FOUND.getMessage())
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleConflict_shouldReturn409() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleConflict(
                new ConflictException(ConstMessagesEnum.EMAIL_ALREADY_EXISTS.getMessage())
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleUnauthorized_shouldReturn401() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleUnauthorized(
                new UnauthorizedException(ConstMessagesEnum.INVALID_CREDENTIALS.getMessage())
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleForbidden_shouldReturn403() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleForbidden(
                new ForbiddenException(ConstMessagesEnum.ACCESS_DENIED.getMessage())
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400WithErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new AuthLoginRequestDto(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));

        MethodParameter parameter = new MethodParameter(
                AuthController.class.getDeclaredMethod("login", AuthLoginRequestDto.class),
                0
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<HttpExceptionMessage> response = handler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getErrors());
        assertEquals("must not be blank", response.getBody().getErrors().get("email"));
    }

    @Test
    void handleDuplicateKey_shouldReturn409() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleDuplicateKey(
                new DuplicateKeyException("duplicate key")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ConstMessagesEnum.EMAIL_ALREADY_EXISTS.getMessage(), response.getBody().getMessage());
    }

    @Test
    void handleException_shouldReturn500() {
        ResponseEntity<HttpExceptionMessage> response = handler.handleException(
                new RuntimeException("unexpected")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ConstMessagesEnum.INTERNAL_ERROR.getMessage(), response.getBody().getMessage());
    }
}
