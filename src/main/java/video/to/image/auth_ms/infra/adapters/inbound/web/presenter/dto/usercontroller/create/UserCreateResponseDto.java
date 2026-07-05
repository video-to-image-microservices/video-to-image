package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateResponseDto {
    private UUID id;
    private String name;
    private String email;
}
