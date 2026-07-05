package video.to.image.auth_ms.core.application.ports.out;

public interface PasswordEncoderOutputPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
