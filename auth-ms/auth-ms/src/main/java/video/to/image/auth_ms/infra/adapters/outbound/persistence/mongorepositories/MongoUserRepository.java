package video.to.image.auth_ms.infra.adapters.outbound.persistence.mongorepositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.documents.MongoUser;

import java.util.Optional;
import java.util.UUID;

public interface MongoUserRepository extends MongoRepository<MongoUser, UUID> {

    Optional<MongoUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
