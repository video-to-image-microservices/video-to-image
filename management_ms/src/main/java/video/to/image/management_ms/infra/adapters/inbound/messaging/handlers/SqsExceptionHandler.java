package video.to.image.management_ms.infra.adapters.inbound.messaging.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Component;
import video.to.image.management_ms.core.domain.exceptions.ConflictException;
import video.to.image.management_ms.infra.adapters.inbound.messaging.events.UserEvent;

@Slf4j
@Component
public class SqsExceptionHandler {

    public void handle(Exception ex, UserEvent event, String queueName) throws Exception {
        if (ex instanceof ConflictException c) {
            // idempotência: usuário já existe → ack, não retry
            log.warn("Idempotent conflict on {}: {}", queueName, event.userId());
        }
        else if (ex instanceof ChangeSetPersister.NotFoundException n) {
            // em evento de delete, pode ser ack (já removido)
            log.info("Already processed: {}", event.userId());
        }
//        else if (ex instanceof IllegalArgumentException || ex instanceof JsonProcessingException) {
//            // poison message → DLQ ou ack definitivo
//            // Nota: O compilador exige o tratamento de exceções checadas como JsonProcessingException aqui
//            throw new NonRetryableMessagingException(ex);
//        }
        else {
            throw ex; // deixa SQS fazer retry
        }
    }
}
