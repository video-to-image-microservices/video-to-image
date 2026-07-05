package video.to.image.auth_ms.infra.broker.publisher;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import video.to.image.auth_ms.infra.broker.events.UserEvent;
import video.to.image.auth_ms.infra.config.SqsProperties;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final SqsTemplate sqsTemplate;
    private final SqsProperties sqsProperties;

    public void publishCreation(UserEvent event) {
        this.sqsTemplate.send(options -> options
                .queue(this.sqsProperties.getUserCreatedQueue())
                .payload(event));
    }

    public void publishDeletion(UserEvent event) {
        this.sqsTemplate.send(options -> options
                .queue(this.sqsProperties.getUserDeletedQueue())
                .payload(event));
    }
}
