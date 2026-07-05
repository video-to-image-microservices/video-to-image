package video.to.image.management_ms.infra.adapters.inbound.messaging.consumers;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.application.ports.in.VideoStatusQueueUseCaseInputPort;
import video.to.image.management_ms.infra.adapters.inbound.messaging.events.VideoStatusEvent;

@Slf4j
@Component
public class VideoStatusSqsConsumer {

    private final VideoStatusQueueUseCaseInputPort videoStatusQueueUseCase;

    public VideoStatusSqsConsumer(VideoStatusQueueUseCaseInputPort videoStatusQueueUseCase) {
        this.videoStatusQueueUseCase = videoStatusQueueUseCase;
    }

    @SqsListener("${app.sqs.status-queue}")
    public void consume(VideoStatusEvent event) {
        log.info("Consumed message from status queue: videoProcessId={}, status={}", event.videoProcessId(), event.status());
        videoStatusQueueUseCase.updateStatus(event.videoProcessId(), event.status(), event.zipStorageKey(), event.zipFileName());
    }
}
