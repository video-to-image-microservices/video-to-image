package video.to.image.auth_ms.infra.adapters.outbound.persistence.adapters;

import org.springframework.stereotype.Component;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.jpaentities.JpaUser;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.jparepositories.JpaUserRepository;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.mappers.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryOutputPortAdapter implements UserRepositoryOutputPort  {
    private final JpaUserRepository repository;
    private final UserMapper mapper;

    public UserRepositoryOutputPortAdapter(JpaUserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        JpaUser jpaUser = this.mapper.toEntity(user);
        return this.mapper.toDomain(this.repository.save(jpaUser));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return this.repository.findById(id).map(this.mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return this.repository.findAll().stream().map(this.mapper::toDomain).toList();
    }

    @Override
    public void delete(User user) {
        this.repository.delete(this.mapper.toEntity(user));
    }

    @Override
    public boolean existsByEmail(String email) {
        return this.repository.existsByEmail(email);
    }
}
