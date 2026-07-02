package video.to.image.management_ms.infra.adapters.inbound.messaging.events;

import video.to.image.management_ms.core.domain.enums.VideoProcessingStatus;

import java.util.UUID;

public record VideoStatusEvent(
        UUID videoProcessId,
        VideoProcessingStatus status,
        String zipStorageKey,
        String zipFileName
) {
}
