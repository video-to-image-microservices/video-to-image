package video.to.image.management_ms.infra.cache;

import java.io.Serializable;

public record CachedStoredVideo(
        byte[] content,
        String contentType,
        long contentLength,
        String fileName
) implements Serializable {
    private static final long serialVersionUID = 1L;
}