package video.to.image.auth_ms.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import video.to.image.auth_ms.core.application.ports.in.AuthenticateUseCaseInputPort;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.TokenGeneratorOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.application.usecases.AuthenticateUseCase;

@Configuration
public class AuthenticateUseCaseConfig {

    @Bean
    AuthenticateUseCaseInputPort authenticateUseCase(
            UserRepositoryOutputPort userRepository,
            PasswordEncoderOutputPort passwordEncoder,
            TokenGeneratorOutputPort tokenGenerator
    ) {
        return new AuthenticateUseCase(userRepository, passwordEncoder, tokenGenerator);
    }
}
