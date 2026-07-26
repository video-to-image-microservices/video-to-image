package video.to.image.worker_ms.messaging

import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import video.to.image.worker_ms.service.VideoProcessingService

@Component
class ProcessVideoConsumer(
    private val videoProcessingService: VideoProcessingService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @SqsListener("\${app.sqs.process-queue}")
    fun consume(event: ProcessVideoEvent) {
        log.info(
            "Consumed process-queue message: videoProcessId={}, storageKey={}",
            event.videoProcessId,
            event.storageKey,
        )
        videoProcessingService.process(event)
    }
}
