package video.to.image.auth_ms.core.application.usecases;

import video.to.image.auth_ms.core.application.ports.in.UserCrudUseCaseInputPort;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.core.domain.enums.ConstMessagesEnum;
import video.to.image.auth_ms.core.domain.exceptions.ConflictException;
import video.to.image.auth_ms.core.domain.exceptions.NotFoundException;

import java.util.List;
import java.util.UUID;

public class UserCrudUseCase implements UserCrudUseCaseInputPort {

    private final UserRepositoryOutputPort userRepository;

    public UserCrudUseCase(UserRepositoryOutputPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findById(UUID id) {
        return this.userRepository.findById(id).orElseThrow(() -> new NotFoundException(ConstMessagesEnum.NOT_FOUND.getMessagem()));
    }

    @Override
    public User create(User user) {
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException(ConstMessagesEnum.EMAIL_ALREADY_EXISTS.getMessagem());
        }
        return this.userRepository.save(user);
    }

    @Override
    public User update(UUID userid, User user) {
        User persistedUser = this.findById(userid);
        persistedUser.setName(user.getName());
        return this.userRepository.save(persistedUser);
    }

    @Override
    public void delete(UUID userid) {
        User persistedUser = this.findById(userid);
        this.userRepository.delete(persistedUser);
    }

    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }
}
