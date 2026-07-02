package video.to.image.management_ms.infra.adapters.outbound.messaging.events;

import java.util.UUID;

public record ProcessVideoEvent(
        UUID videoProcessId,
        UUID userId,
        String bucket,
        String storageKey,
        String outputZipKey,
        String originalFileName,
        String contentType
) {
}
