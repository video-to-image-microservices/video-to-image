package video.to.image.auth_ms.infra.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqsProperties.class)
public class SqsConfig {
}
