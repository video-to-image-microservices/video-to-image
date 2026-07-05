package video.to.image.auth_ms.infra.adapters.inbound.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.exceptions.HttpExceptionMessage;

import java.io.IOException;

final class SecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private SecurityErrorResponseWriter() {}

    static void write(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        HttpExceptionMessage body = new HttpExceptionMessage(status, message);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), body);
    }
}
