package video.to.image.management_ms.infra.adapters.inbound.messaging.consumers;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.application.ports.in.UserQueuesUseCaseInputPort;
import video.to.image.management_ms.infra.adapters.inbound.messaging.events.UserEvent;
import video.to.image.management_ms.infra.adapters.inbound.messaging.handlers.SqsExceptionHandler;

@Slf4j
@Component
public class UserEventSqsConsumer {

    private final UserQueuesUseCaseInputPort userQueuesUseCase;
    private final SqsExceptionHandler sqsExceptionHandler;
    private final String userCreatedQueue;
    private final String userDeletedQueue;

    public UserEventSqsConsumer(
            UserQueuesUseCaseInputPort userQueuesUseCase,
            SqsExceptionHandler sqsExceptionHandler,
            @Value("${app.sqs.user-created-queue}") String userCreatedQueue,
            @Value("${app.sqs.user-deleted-queue}") String userDeletedQueue
    ) {
        this.userQueuesUseCase = userQueuesUseCase;
        this.sqsExceptionHandler = sqsExceptionHandler;
        this.userCreatedQueue = userCreatedQueue;
        this.userDeletedQueue = userDeletedQueue;
    }

    @SqsListener("${app.sqs.user-created-queue}")
    public void consumeUserCreated(UserEvent event) throws Exception {
        log.info("Consumed message from {}: userId={}", userCreatedQueue, event.userId());
        try {
            userQueuesUseCase.processNewUserEvent(event.userId());
        } catch (Exception e) {
            this.sqsExceptionHandler.handle(e, event, userCreatedQueue);
        }
    }

    @SqsListener("${app.sqs.user-deleted-queue}")
    public void consumeUserDeleted(UserEvent event) throws Exception {
        log.info("Consumed message from {}: userId={}", userDeletedQueue, event.userId());
        try {
            userQueuesUseCase.processDeletedUserEvent(event.userId());
        } catch (Exception e) {
            this.sqsExceptionHandler.handle(e, event, userDeletedQueue);
        }
    }
}
