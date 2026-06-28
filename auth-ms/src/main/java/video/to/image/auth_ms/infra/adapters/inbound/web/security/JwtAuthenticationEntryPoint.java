package video.to.image.auth_ms.infra.adapters.inbound.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        SecurityErrorResponseWriter.write(
                response,
                HttpStatus.UNAUTHORIZED.value(),
                ConstMessagesEnum.INVALID_CREDENTIALS.getMessagem()
        );
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        SecurityErrorResponseWriter.write(
                response,
                HttpStatus.FORBIDDEN.value(),
                ConstMessagesEnum.ACCESS_DENIED.getMessagem()
        );
    }
}
