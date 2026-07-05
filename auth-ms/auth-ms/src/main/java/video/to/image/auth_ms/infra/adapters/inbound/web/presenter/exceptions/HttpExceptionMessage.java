package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class HttpExceptionMessage {

    private Integer status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public HttpExceptionMessage(Integer status, String message) {
        this(status, message, null);
    }

    public HttpExceptionMessage(Integer status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
