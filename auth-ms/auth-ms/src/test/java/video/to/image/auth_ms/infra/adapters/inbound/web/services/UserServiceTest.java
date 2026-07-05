package video.to.image.auth_ms.infra.adapters.inbound.web.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import video.to.image.auth_ms.core.application.ports.in.UserCrudUseCaseInputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update.UserUpdateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.SecurityContextUtils;
import video.to.image.auth_ms.infra.broker.events.UserEvent;
import video.to.image.auth_ms.infra.broker.publisher.UserEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserCrudUseCaseInputPort userUseCases;

    @Mock
    private UserEventPublisher publisher;

    @InjectMocks
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_shouldPublishCreationEvent() {
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .name("João")
                .email("joao@email.com")
                .password("senha123")
                .build();

        User savedUser = new User(userId, "João", "joao@email.com", "hashed");
        when(userUseCases.create(any(User.class))).thenReturn(savedUser);

        UserCreateResponseDto result = userService.create(request);

        assertEquals(userId, result.getId());
        assertEquals("joao@email.com", result.getEmail());
        verify(publisher).publishCreation(new UserEvent(userId));
    }

    @Test
    void findById_shouldPassRequesterIdFromSecurityContext() {
        User user = new User(userId, "João", "joao@email.com", "hashed");

        try (MockedStatic<SecurityContextUtils> securityContext = mockStatic(SecurityContextUtils.class)) {
            securityContext.when(SecurityContextUtils::getCurrentUserId).thenReturn(userId);
            when(userUseCases.findById(userId, userId)).thenReturn(user);

            UserCreateResponseDto result = userService.findById(userId);

            assertEquals(userId, result.getId());
            verify(userUseCases).findById(userId, userId);
        }
    }

    @Test
    void update_shouldPassRequesterIdFromSecurityContext() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .name("João Atualizado")
                .build();
        User updatedUser = new User(userId, "João Atualizado", "joao@email.com", "hashed");

        try (MockedStatic<SecurityContextUtils> securityContext = mockStatic(SecurityContextUtils.class)) {
            securityContext.when(SecurityContextUtils::getCurrentUserId).thenReturn(userId);
            when(userUseCases.update(eq(userId), eq(userId), any(User.class))).thenReturn(updatedUser);

            UserCreateResponseDto result = userService.update(userId, request);

            assertEquals("João Atualizado", result.getName());
            verify(userUseCases).update(eq(userId), eq(userId), any(User.class));
        }
    }

    @Test
    void delete_shouldPublishDeletionEvent() {
        try (MockedStatic<SecurityContextUtils> securityContext = mockStatic(SecurityContextUtils.class)) {
            securityContext.when(SecurityContextUtils::getCurrentUserId).thenReturn(userId);

            userService.delete(userId);

            verify(userUseCases).delete(userId, userId);
            verify(publisher).publishDeletion(new UserEvent(userId));
        }
    }
}
