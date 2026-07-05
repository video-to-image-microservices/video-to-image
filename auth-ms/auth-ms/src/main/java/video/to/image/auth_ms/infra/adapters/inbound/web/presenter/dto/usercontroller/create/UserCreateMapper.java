package video.to.image.auth_ms.infra.adapters.inbound.web.presenter.dto.usercontroller.create;


import video.to.image.auth_ms.core.domain.entities.User;

public class UserCreateMapper {

    public static User toDomain(UserCreateRequestDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        return user;
    }

    public static UserCreateResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserCreateResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .id(user.getId())
                .build();
    }
}
