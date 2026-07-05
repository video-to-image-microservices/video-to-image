package video.to.image.auth_ms.core.application.ports.in;

import video.to.image.auth_ms.core.domain.entities.User;

import java.util.UUID;

public interface UserCrudUseCaseInputPort {
    User findById(UUID requesterId, UUID id);
    User create(User user);
    User update(UUID requesterId, UUID userid, User user);
    void delete(UUID requesterId, UUID userid);
}
