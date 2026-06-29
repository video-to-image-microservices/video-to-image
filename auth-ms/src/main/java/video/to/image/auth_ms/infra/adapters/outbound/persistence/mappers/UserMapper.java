package video.to.image.auth_ms.infra.adapters.outbound.persistence.mappers;

import org.mapstruct.Mapper;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.adapters.outbound.persistence.documents.MongoUser;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(MongoUser entity);
    MongoUser toEntity(User user);
}
