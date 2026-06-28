package video.to.image.auth_ms.core.application.usecases;

import video.to.image.auth_ms.core.application.ports.in.UserCrudUseCaseInputPort;
import video.to.image.auth_ms.core.application.ports.out.PasswordEncoderOutputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ConflictException;
import video.to.image.auth_ms.core.domain.exceptions.ForbiddenException;
import video.to.image.auth_ms.core.domain.exceptions.NotFoundException;

import java.util.UUID;

public class UserCrudUseCase implements UserCrudUseCaseInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final PasswordEncoderOutputPort passwordEncoder;

    public UserCrudUseCase(UserRepositoryOutputPort userRepository, PasswordEncoderOutputPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findById(UUID requesterId, UUID id) {
        this.assertOwnership(requesterId, id);
        return this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(ConstMessagesEnum.NOT_FOUND.getMessagem()));
    }

    @Override
    public User create(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException(ConstMessagesEnum.EMAIL_ALREADY_EXISTS.getMessagem());
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    @Override
    public User update(UUID requesterId, UUID userid, User user) {
        this.assertOwnership(requesterId, userid);
        User persistedUser = this.userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException(ConstMessagesEnum.NOT_FOUND.getMessagem()));
        persistedUser.setName(user.getName());
        return this.userRepository.save(persistedUser);
    }

    @Override
    public void delete(UUID requesterId, UUID userid) {
        this.assertOwnership(requesterId, userid);
        User persistedUser = this.userRepository.findById(userid)
                .orElseThrow(() -> new NotFoundException(ConstMessagesEnum.NOT_FOUND.getMessagem()));
        this.userRepository.delete(persistedUser);
    }

    private void assertOwnership(UUID requesterId, UUID resourceId) {
        if (!requesterId.equals(resourceId)) {
            throw new ForbiddenException(ConstMessagesEnum.ACCESS_DENIED.getMessagem());
        }
    }
}
