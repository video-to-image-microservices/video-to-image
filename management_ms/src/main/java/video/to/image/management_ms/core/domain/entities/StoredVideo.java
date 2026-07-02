package video.to.image.management_ms.core.domain.entities;

import java.io.InputStream;

public record StoredVideo(
        InputStream content,
        String contentType,
        long contentLength,
        String fileName
) {
}
