package video.to.image.management_ms.infra.adapters.inbound.messaging.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.domain.exceptions.ConflictException;
import video.to.image.management_ms.infra.adapters.inbound.messaging.events.UserEvent;

@Slf4j
@Component
public class SqsExceptionHandler {

    public void handle(Exception ex, UserEvent event, String queueName) throws Exception {
        if (ex instanceof ConflictException) {
            log.warn("Idempotent conflict on {}: {}", queueName, event.userId());
            return;
        }
        throw ex;
    }
}
