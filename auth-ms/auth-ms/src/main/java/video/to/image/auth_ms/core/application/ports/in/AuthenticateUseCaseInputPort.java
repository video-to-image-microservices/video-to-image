package video.to.image.auth_ms.core.application.ports.in;

import video.to.image.auth_ms.core.domain.entities.AuthResult;

public interface AuthenticateUseCaseInputPort {
    AuthResult authenticate(String email, String password);
}
