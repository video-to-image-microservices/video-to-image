package video.to.image.management_ms.infra.adapters.inbound.web.dtos;

import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.util.UUID;

public record VideoUploadResponse(
        UUID videoProcessId,
        UUID userId,
        String fileName,
        String zipFileName,
        String zipStorageKey,
        VideoProcessingStatus status
) {
}
