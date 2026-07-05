package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login;

import video.to.image.auth_ms.core.domain.entities.AuthResult;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateMapper;

public class AuthLoginMapper {

    private AuthLoginMapper() {}

    public static AuthLoginResponseDto toResponseDto(AuthResult authResult) {
        if (authResult == null) {
            return null;
        }

        return AuthLoginResponseDto.builder()
                .token(authResult.getToken())
                .user(UserCreateMapper.toResponseDto(authResult.getUser()))
                .build();
    }
}
