package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.update;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {
    @NotEmpty
    private String name;
}
