package video.to.image.auth_ms.infra.adapters.inbound.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;

import java.util.UUID;

public final class SecurityContextUtils {

    private SecurityContextUtils() {}

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException(ConstMessagesEnum.INVALID_CREDENTIALS.getMessagem());
        }

        try {
            return UUID.fromString(authentication.getPrincipal().toString());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(ConstMessagesEnum.INVALID_CREDENTIALS.getMessagem());
        }
    }
}
