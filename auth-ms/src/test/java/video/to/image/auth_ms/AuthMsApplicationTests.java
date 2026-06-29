package video.to.image.auth_ms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.mongorepositories.MongoUserRepository;
import video.to.image.auth_ms.infra.broker.publisher.UserEventPublisher;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-with-at-least-32-characters",
        "spring.cloud.aws.sqs.enabled=false",
        "mongock.enabled=false"
})
class AuthMsApplicationTests {

    @MockitoBean
    private UserEventPublisher userEventPublisher;

    @MockitoBean
    private MongoUserRepository mongoUserRepository;

    @Test
    void contextLoads() {
    }
}
