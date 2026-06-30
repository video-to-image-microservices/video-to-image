package video.to.image.management_ms.core.application.ports.out;

import video.to.image.management_ms.core.domain.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryOutputPort {
    User save(User user);
    Optional<User> findById(UUID id);
    void delete(User user);
    boolean existsById(UUID id);
}
