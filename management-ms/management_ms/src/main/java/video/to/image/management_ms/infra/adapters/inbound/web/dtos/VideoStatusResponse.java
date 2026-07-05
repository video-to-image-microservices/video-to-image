package video.to.image.management_ms.infra.adapters.inbound.web.dtos;

import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.time.Instant;
import java.util.UUID;

public record VideoStatusResponse(
        UUID videoProcessId,
        UUID userId,
        String fileName,
        String zipFileName,
        String zipStorageKey,
        VideoProcessingStatus status,
        Instant updatedAt
) {
}
