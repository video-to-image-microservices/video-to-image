package video.to.image.management_ms.infra.adapters.outbound.persistence.mappers;

import video.to.image.management_ms.core.domain.entities.User;
import video.to.image.management_ms.infra.adapters.outbound.persistence.jpaentities.JpaUser;

public class JpaUserMapper {
    public static JpaUser UserToJpaUser(User u) {
        return new JpaUser(u.getId());
    }

    public static User JpaUserToUser(JpaUser u) {
        return new User(u.getId());
    }
}
