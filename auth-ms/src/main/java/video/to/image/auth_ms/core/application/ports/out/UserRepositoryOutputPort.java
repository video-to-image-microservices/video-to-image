package video.to.image.auth_ms.core.application.ports.out;

import video.to.image.auth_ms.core.domain.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryOutputPort {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findAll();
    void delete(User user);
    boolean existsByEmail(String email);
}
