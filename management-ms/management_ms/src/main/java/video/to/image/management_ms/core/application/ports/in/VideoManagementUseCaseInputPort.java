package video.to.image.management_ms.core.application.ports.in;

import video.to.image.management_ms.core.domain.entities.StoredVideo;
import video.to.image.management_ms.core.domain.entities.VideoProcess;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface VideoManagementUseCaseInputPort {
    VideoProcess upload(UUID userId, String originalFileName, String contentType, long fileSize, InputStream content);

    StoredVideo download(UUID userId, String originalFileName);

    VideoProcess getStatus(UUID userId, String originalFileName);

    List<VideoProcess> listStatus(UUID userId);
}
