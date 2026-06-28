package video.to.image.auth_ms.infra.adapters.inbound.web.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import video.to.image.auth_ms.core.application.ports.in.UserCrudUseCaseInputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateMapper;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update.UserUpdateMapper;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update.UserUpdateRequestDto;
import video.to.image.auth_ms.infra.adapters.inbound.web.security.SecurityContextUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserCrudUseCaseInputPort userUseCases;
    // TODO config SQS
//    private final UserEventPublisher publisher;

    @Transactional
    public UserCreateResponseDto create(UserCreateRequestDto body) {
        User user = this.userUseCases.create(UserCreateMapper.toDomain(body));
//        this.publisher.publishCreation(new UserEvent(user.getId()));
        return UserCreateMapper.toResponseDto(user);
    }

    @Transactional
    public UserCreateResponseDto update(UUID id, UserUpdateRequestDto body) {
        UUID requesterId = SecurityContextUtils.getCurrentUserId();
        User user = this.userUseCases.update(requesterId, id, UserUpdateMapper.toDomain(body));
        return UserCreateMapper.toResponseDto(user);
    }

    public UserCreateResponseDto findById(UUID id) {
        UUID requesterId = SecurityContextUtils.getCurrentUserId();
        User user = this.userUseCases.findById(requesterId, id);
        return UserCreateMapper.toResponseDto(user);
    }

    @Transactional
    public void delete(UUID id) {
        UUID requesterId = SecurityContextUtils.getCurrentUserId();
        this.userUseCases.delete(requesterId, id);
        //        this.publisher.publishDeletion(new UserEvent(id));
    }
}
