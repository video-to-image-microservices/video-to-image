package video.to.image.management_ms.infra.adapters.outbound.persistence.adapters;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import video.to.image.management_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.management_ms.core.domain.entities.User;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jpaentities.JpaUser;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jparepositories.JpaUserRepository;
import video.to.image.management_ms.infra.adapters.outbound.persistence.mappers.JpaUserMapper;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryOutputPortAdapter implements UserRepositoryOutputPort {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryOutputPortAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    @Transactional
    public User save(User user) {
        JpaUser jpaUser = JpaUserMapper.UserToJpaUser(user);
        return JpaUserMapper.JpaUserToUser(this.jpaUserRepository.save(jpaUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return this.jpaUserRepository.findById(id).map(JpaUserMapper::JpaUserToUser);
    }

    @Override
    @Transactional
    public void delete(User user) {
        this.jpaUserRepository.deleteById(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return this.jpaUserRepository.existsById(id);
    }
}
