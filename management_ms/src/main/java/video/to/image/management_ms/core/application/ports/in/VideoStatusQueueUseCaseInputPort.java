package video.to.image.management_ms.core.application.ports.in;

import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.util.UUID;

public interface VideoStatusQueueUseCaseInputPort {
    void updateStatus(UUID videoProcessId, VideoProcessingStatus status, String zipStorageKey, String zipFileName);
}
