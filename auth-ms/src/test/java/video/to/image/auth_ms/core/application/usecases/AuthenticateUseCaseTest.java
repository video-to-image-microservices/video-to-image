package video.to.image.auth_ms.core.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.TokenGeneratorOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.AuthResult;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.UnauthorizedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;

    @Mock
    private PasswordEncoderOutputPort passwordEncoder;

    @Mock
    private TokenGeneratorOutputPort tokenGenerator;

    @InjectMocks
    private AuthenticateUseCase authenticateUseCase;

    private User user;
    private static final String EMAIL = "user@email.com";
    private static final String RAW_PASSWORD = "senha123";
    private static final String HASHED_PASSWORD = "$2a$10$hashed";
    private static final String TOKEN = "jwt-token";

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "João", EMAIL, HASHED_PASSWORD);
    }

    @Test
    void authenticate_shouldReturnAuthResult_whenCredentialsAreValid() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(tokenGenerator.generate(user)).thenReturn(TOKEN);

        AuthResult result = authenticateUseCase.authenticate(EMAIL, RAW_PASSWORD);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(TOKEN, result.getToken());
        verify(tokenGenerator).generate(user);
    }

    @Test
    void authenticate_shouldThrowUnauthorizedException_whenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authenticateUseCase.authenticate(EMAIL, RAW_PASSWORD)
        );

        assertEquals(ConstMessagesEnum.INVALID_CREDENTIALS.getMessage(), exception.getMessage());
        verify(passwordEncoder, never()).matches(RAW_PASSWORD, HASHED_PASSWORD);
        verify(tokenGenerator, never()).generate(user);
    }

    @Test
    void authenticate_shouldThrowUnauthorizedException_whenPasswordDoesNotMatch() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authenticateUseCase.authenticate(EMAIL, RAW_PASSWORD)
        );

        assertEquals(ConstMessagesEnum.INVALID_CREDENTIALS.getMessage(), exception.getMessage());
        verify(tokenGenerator, never()).generate(user);
    }
}
