package video.to.image.auth_ms.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import video.to.image.auth_ms.core.application.ports.in.UserCrudUseCaseInputPort;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.application.usecases.UserCrudUseCase;

@Configuration
public class UserCrudUseCaseConfig {

    @Bean
    UserCrudUseCaseInputPort UserCrudUseCase(
            UserRepositoryOutputPort repository,
            PasswordEncoderOutputPort passwordEncoder
    ) {
        return new UserCrudUseCase(repository, passwordEncoder);
    }
}
