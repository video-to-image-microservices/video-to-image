package video.to.image.management_ms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.cloud.aws.sqs.enabled=false")
class ManagementMsApplicationTests {

	@Test
	void contextLoads() {
	}

}
