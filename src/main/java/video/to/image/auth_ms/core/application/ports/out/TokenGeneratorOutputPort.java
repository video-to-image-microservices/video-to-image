package video.to.image.auth_ms.core.application.ports.out;

import video.to.image.auth_ms.core.domain.entities.User;

public interface TokenGeneratorOutputPort {
    String generate(User user);
}
