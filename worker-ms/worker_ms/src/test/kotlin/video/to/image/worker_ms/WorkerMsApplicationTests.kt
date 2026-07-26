package video.to.image.worker_ms

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client

@SpringBootTest(properties = ["spring.cloud.aws.sqs.enabled=false"])
class WorkerMsApplicationTests {

    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var sqsTemplate: SqsTemplate

    @Test
    fun contextLoads() {
    }
}
