package video.to.image.management_ms.core.application.ports.in;

import video.to.image.management_ms.core.domain.entities.User;

import java.util.UUID;

public interface UserQueuesUseCaseInputPort {
    User processNewUserEvent(UUID userId);
    void processDeletedUserEvent(UUID userId);
}
