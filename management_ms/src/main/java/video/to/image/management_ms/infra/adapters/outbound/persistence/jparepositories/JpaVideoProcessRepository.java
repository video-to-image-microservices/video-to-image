package video.to.image.management_ms.infra.adapters.outbound.persistence.jparepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jpaentities.JpaVideoProcess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaVideoProcessRepository extends JpaRepository<JpaVideoProcess, UUID> {
    Optional<JpaVideoProcess> findTopByUserIdAndOriginalFileNameOrderByCreatedAtDesc(UUID userId, String originalFileName);

    List<JpaVideoProcess> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
