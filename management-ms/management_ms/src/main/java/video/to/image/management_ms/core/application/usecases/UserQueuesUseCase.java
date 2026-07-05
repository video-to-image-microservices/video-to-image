package video.to.image.management_ms.core.application.usecases;

import video.to.image.management_ms.core.application.ports.in.UserQueuesUseCaseInputPort;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.domain.entities.User;
import video.to.image.management_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.management_ms.core.domain.exceptions.ConflictException;

import java.util.UUID;

public class UserQueuesUseCase implements UserQueuesUseCaseInputPort {

    private final UserRepositoryOutputPort userRepository;

    public UserQueuesUseCase(UserRepositoryOutputPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User processNewUserEvent(UUID userId) {
        if (this.userRepository.existsById(userId))
            throw new ConflictException(ConstMessagesEnum.USER_CONFLICT.getMessage());

        User newUser = new User();
        newUser.setId(userId);
        return userRepository.save(newUser);
    }

    @Override
    public void processDeletedUserEvent(UUID userId) {
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }
}
