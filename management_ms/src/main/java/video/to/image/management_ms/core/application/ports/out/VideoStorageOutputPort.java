package video.to.image.management_ms.core.application.ports.out;

import video.to.image.management_ms.core.domain.entities.StoredVideo;

import java.io.InputStream;

public interface VideoStorageOutputPort {
    void upload(String key, InputStream content, long contentLength, String contentType);

    StoredVideo download(String key, String fileName);
}
