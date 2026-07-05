package video.to.image.management_ms.core.application.ports.out;

import video.to.image.management_ms.core.domain.entities.VideoProcess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoProcessRepositoryOutputPort {
    VideoProcess save(VideoProcess videoProcess);

    Optional<VideoProcess> findByUserIdAndOriginalFileName(UUID userId, String originalFileName);

    List<VideoProcess> findByUserId(UUID userId);

    Optional<VideoProcess> findById(UUID id);
}
