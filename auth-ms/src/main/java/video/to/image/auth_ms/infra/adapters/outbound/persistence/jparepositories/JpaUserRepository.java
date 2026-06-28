package video.to.image.auth_ms.infra.adapters.outbound.persistence.jparepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.jpaentities.JpaUser;

import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<JpaUser, UUID> {
    boolean existsByEmail(String email);
}
