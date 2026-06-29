package video.to.image.auth_ms.core.application.usecases;

import video.to.image.auth_ms.core.application.ports.in.AuthenticateUseCaseInputPort;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.TokenGeneratorOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.AuthResult;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;

public class AuthenticateUseCase implements AuthenticateUseCaseInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final PasswordEncoderOutputPort passwordEncoder;
    private final TokenGeneratorOutputPort tokenGenerator;

    public AuthenticateUseCase(
            UserRepositoryOutputPort userRepository,
            PasswordEncoderOutputPort passwordEncoder,
            TokenGeneratorOutputPort tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public AuthResult authenticate(String email, String password) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(ConstMessagesEnum.INVALID_CREDENTIALS.getMessage()));

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException(ConstMessagesEnum.INVALID_CREDENTIALS.getMessage());
        }

        String token = this.tokenGenerator.generate(user);
        return new AuthResult(user, token);
    }
}
