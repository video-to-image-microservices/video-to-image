package video.to.image.auth_ms.infra.broker.publisher;

import io.awspring.cloud.sqs.operations.SqsSendOptions;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import video.to.image.auth_ms.infra.broker.events.UserEvent;
import video.to.image.auth_ms.infra.config.SqsProperties;

import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEventPublisherTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private SqsProperties sqsProperties;

    @InjectMocks
    private UserEventPublisher userEventPublisher;

    private UserEvent userEvent;

    @BeforeEach
    void setUp() {
        userEvent = new UserEvent(UUID.randomUUID());
        lenient().when(sqsProperties.getUserCreatedQueue()).thenReturn("user-created-queue");
        lenient().when(sqsProperties.getUserDeletedQueue()).thenReturn("user-deleted-queue");

        SqsSendOptions sendOptions = mock(SqsSendOptions.class, RETURNS_SELF);
        doAnswer(invocation -> {
            Consumer<SqsSendOptions> consumer = invocation.getArgument(0);
            consumer.accept(sendOptions);
            return null;
        }).when(sqsTemplate).send(any());
    }

    @Test
    void publishCreation_shouldSendToUserCreatedQueue() {
        userEventPublisher.publishCreation(userEvent);

        verify(sqsTemplate).send(any());
        verify(sqsProperties).getUserCreatedQueue();
    }

    @Test
    void publishDeletion_shouldSendToUserDeletedQueue() {
        userEventPublisher.publishDeletion(userEvent);

        verify(sqsTemplate).send(any());
        verify(sqsProperties).getUserDeletedQueue();
    }
}
