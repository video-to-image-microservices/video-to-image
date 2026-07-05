package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.authcontroller.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create.UserCreateResponseDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponseDto {

    private String token;
    private UserCreateResponseDto user;
}
