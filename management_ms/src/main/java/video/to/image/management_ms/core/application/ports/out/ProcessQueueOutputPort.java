package video.to.image.management_ms.core.application.ports.out;

import video.to.image.management_ms.core.domain.entities.VideoProcess;

public interface ProcessQueueOutputPort {
    void publish(VideoProcess videoProcess);
}
