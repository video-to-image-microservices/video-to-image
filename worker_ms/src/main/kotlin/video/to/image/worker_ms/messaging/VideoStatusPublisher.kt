package video.to.image.worker_ms.messaging

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class VideoStatusPublisher(
    private val sqsTemplate: SqsTemplate,
    @Value("\${app.sqs.status-queue}") private val statusQueue: String,
) {

    fun publish(event: VideoStatusEvent) {
        sqsTemplate.send(statusQueue, event)
    }
}
