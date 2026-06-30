package video.to.image.management_ms.infra.adapters.inbound.messaging.consumers;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.application.ports.in.UserQueuesUseCaseInputPort;
import video.to.image.management_ms.infra.adapters.inbound.messaging.events.UserEvent;
import video.to.image.management_ms.infra.adapters.inbound.messaging.handlers.SqsExceptionHandler;

@Slf4j
@Component
public class UserEventSqsConsumer {

    private final UserQueuesUseCaseInputPort userQueuesUseCase;
    private final SqsExceptionHandler sqsExceptionHandler;

    public UserEventSqsConsumer(UserQueuesUseCaseInputPort userQueuesUseCase, SqsExceptionHandler sqsExceptionHandler) {
        this.userQueuesUseCase = userQueuesUseCase;
        this.sqsExceptionHandler = sqsExceptionHandler;
    }

    @SqsListener("${app.sqs.user-created-queue}")
    public void consumeUserCreated(UserEvent event) throws Exception {
        log.info("Consumed message from user-created-queue: userId={}", event.userId());
        try {
            userQueuesUseCase.processNewUserEvent(event.userId());
        } catch (Exception e) {
            //TODO recuperar nome da fila acessando as variáveis de ambiente
            this.sqsExceptionHandler.handle(e, event, "user-created-queue");
        }
    }

    @SqsListener("${app.sqs.user-deleted-queue}")
    public void consumeUserDeleted(UserEvent event) throws Exception {
        log.info("Consumed message from user-deleted-queue: userId={}", event.userId());
        try {
            userQueuesUseCase.processDeletedUserEvent(event.userId());
        } catch (Exception e) {
            //TODO recuperar nome da fila acessando as variáveis de ambiente
            this.sqsExceptionHandler.handle(e, event, "user-deleted-queue");
        }
    }
}
