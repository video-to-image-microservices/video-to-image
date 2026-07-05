package video.to.image.auth_ms.core.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ConflictException;
import video.to.image.auth_ms.core.domain.exceptions.ForbiddenException;
import video.to.image.auth_ms.core.domain.exceptions.NotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCrudUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;

    @Mock
    private PasswordEncoderOutputPort passwordEncoder;

    @InjectMocks
    private UserCrudUseCase userCrudUseCase;

    private UUID userId;
    private User persistedUser;
    private static final String EMAIL = "user@email.com";
    private static final String RAW_PASSWORD = "senha123";
    private static final String HASHED_PASSWORD = "$2a$10$hashed";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        persistedUser = new User(userId, "João", EMAIL, HASHED_PASSWORD);
    }

    @Test
    void create_shouldEncodePasswordAndSaveUser_whenEmailIsUnique() {
        User newUser = new User(null, "Maria", "maria@email.com", RAW_PASSWORD);
        User savedUser = new User(UUID.randomUUID(), "Maria", "maria@email.com", HASHED_PASSWORD);

        when(userRepository.existsByEmail("maria@email.com")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.save(newUser)).thenReturn(savedUser);

        User result = userCrudUseCase.create(newUser);

        assertEquals(savedUser, result);
        assertEquals(HASHED_PASSWORD, newUser.getPassword());
        assertNotNull(newUser.getId());
        verify(userRepository).save(newUser);
    }

    @Test
    void create_shouldThrowConflictException_whenEmailAlreadyExists() {
        User newUser = new User(null, "Maria", EMAIL, RAW_PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userCrudUseCase.create(newUser)
        );

        assertEquals(ConstMessagesEnum.EMAIL_ALREADY_EXISTS.getMessage(), exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnUser_whenRequesterIsOwner() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(persistedUser));

        User result = userCrudUseCase.findById(userId, userId);

        assertEquals(persistedUser, result);
    }

    @Test
    void findById_shouldThrowForbiddenException_whenRequesterIsNotOwner() {
        UUID otherUserId = UUID.randomUUID();

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userCrudUseCase.findById(otherUserId, userId)
        );

        assertEquals(ConstMessagesEnum.ACCESS_DENIED.getMessage(), exception.getMessage());
        verify(userRepository, never()).findById(userId);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCrudUseCase.findById(userId, userId)
        );

        assertEquals(ConstMessagesEnum.NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    void update_shouldUpdateName_whenRequesterIsOwner() {
        User updateData = new User(null, "João Atualizado", null, null);
        User updatedUser = new User(userId, "João Atualizado", EMAIL, HASHED_PASSWORD);

        when(userRepository.findById(userId)).thenReturn(Optional.of(persistedUser));
        when(userRepository.save(persistedUser)).thenReturn(updatedUser);

        User result = userCrudUseCase.update(userId, userId, updateData);

        assertEquals("João Atualizado", persistedUser.getName());
        assertEquals(updatedUser, result);
        verify(userRepository).save(persistedUser);
    }

    @Test
    void update_shouldThrowForbiddenException_whenRequesterIsNotOwner() {
        UUID otherUserId = UUID.randomUUID();
        User updateData = new User(null, "João Atualizado", null, null);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userCrudUseCase.update(otherUserId, userId, updateData)
        );

        assertEquals(ConstMessagesEnum.ACCESS_DENIED.getMessage(), exception.getMessage());
        verify(userRepository, never()).findById(userId);
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserDoesNotExist() {
        User updateData = new User(null, "João Atualizado", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCrudUseCase.update(userId, userId, updateData)
        );

        assertEquals(ConstMessagesEnum.NOT_FOUND.getMessage(), exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUser_whenRequesterIsOwner() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(persistedUser));

        userCrudUseCase.delete(userId, userId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(captor.capture());
        assertEquals(persistedUser, captor.getValue());
    }

    @Test
    void delete_shouldThrowForbiddenException_whenRequesterIsNotOwner() {
        UUID otherUserId = UUID.randomUUID();

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userCrudUseCase.delete(otherUserId, userId)
        );

        assertEquals(ConstMessagesEnum.ACCESS_DENIED.getMessage(), exception.getMessage());
        verify(userRepository, never()).findById(userId);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCrudUseCase.delete(userId, userId)
        );

        assertEquals(ConstMessagesEnum.NOT_FOUND.getMessage(), exception.getMessage());
        verify(userRepository, never()).delete(any());
    }
}
