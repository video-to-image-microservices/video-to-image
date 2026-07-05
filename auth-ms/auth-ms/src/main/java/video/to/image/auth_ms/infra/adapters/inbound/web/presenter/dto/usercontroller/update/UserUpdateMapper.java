package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update;

import video.to.image.auth_ms.core.domain.entities.User;

public class UserUpdateMapper {

    public static User toDomain(UserUpdateRequestDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());

        return user;
    }
}
