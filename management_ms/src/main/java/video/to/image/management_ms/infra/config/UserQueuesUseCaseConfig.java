package video.to.image.management_ms.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.application.usecases.UserQueuesUseCase;

@Configuration
public class UserQueuesUseCaseConfig {

    @Bean
    public UserQueuesUseCase userQueuesUseCase(UserRepositoryOutputPort userRepositoryOutputPort) {
        return new UserQueuesUseCase(userRepositoryOutputPort);
    }
}
