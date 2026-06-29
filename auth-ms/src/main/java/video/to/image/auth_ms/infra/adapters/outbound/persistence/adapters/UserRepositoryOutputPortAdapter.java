package video.to.image.auth_ms.infra.adapters.outbound.persistence.adapters;

import org.springframework.stereotype.Component;
import video.to.image.auth_ms.core.application.ports.out.UserRepositoryOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.documents.MongoUser;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.mappers.UserMapper;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.mongorepositories.MongoUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryOutputPortAdapter implements UserRepositoryOutputPort {

    private final MongoUserRepository repository;
    private final UserMapper mapper;

    public UserRepositoryOutputPortAdapter(MongoUserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        MongoUser mongoUser = this.mapper.toEntity(user);
        return this.mapper.toDomain(this.repository.save(mongoUser));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return this.repository.findById(id).map(this.mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.repository.findByEmail(email).map(this.mapper::toDomain);
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
