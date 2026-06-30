package video.to.image.management_ms.infra.broker.consumers;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.application.ports.in.UserQueuesUseCaseInputPort;
import video.to.image.management_ms.infra.broker.events.UserEvent;

@Slf4j
@Component
public class UserEventSqsConsumer {

    private final UserQueuesUseCaseInputPort userQueuesUseCase;

    public UserEventSqsConsumer(UserQueuesUseCaseInputPort userQueuesUseCase) {
        this.userQueuesUseCase = userQueuesUseCase;
    }

    @SqsListener("${app.sqs.user-created-queue}")
    public void consumeUserCreated(UserEvent event) {
        log.info("Consumed message from user-created-queue: userId={}", event.userId());
        userQueuesUseCase.processNewUserEvent(event.userId());
    }

    @SqsListener("${app.sqs.user-deleted-queue}")
    public void consumeUserDeleted(UserEvent event) {
        log.info("Consumed message from user-deleted-queue: userId={}", event.userId());
        userQueuesUseCase.processDeletedUserEvent(event.userId());
    }
}
