package video.to.image.management_ms.infra.adapters.outbound.messaging.producers;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.application.ports.out.ProcessQueueOutputPort;
import video.to.image.management_ms.core.domain.entities.VideoProcess;
import video.to.image.management_ms.infra.adapters.outbound.messaging.events.ProcessVideoEvent;

@Component
public class ProcessQueueSqsProducer implements ProcessQueueOutputPort {

    private final SqsTemplate sqsTemplate;
    private final String queueName;
    private final String bucketName;

    public ProcessQueueSqsProducer(
            SqsTemplate sqsTemplate,
            @Value("${app.sqs.process-queue}") String queueName,
            @Value("${app.s3.video-bucket}") String bucketName
    ) {
        this.sqsTemplate = sqsTemplate;
        this.queueName = queueName;
        this.bucketName = bucketName;
    }

    @Override
    public void publish(VideoProcess videoProcess) {
        ProcessVideoEvent event = new ProcessVideoEvent(
                videoProcess.getId(),
                videoProcess.getUserId(),
                bucketName,
                videoProcess.getStorageKey(),
                videoProcess.getZipStorageKey(),
                videoProcess.getOriginalFileName(),
                videoProcess.getContentType()
        );

        sqsTemplate.send(queueName, event);
    }
}
