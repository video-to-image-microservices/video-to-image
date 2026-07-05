package video.to.image.auth_ms.core.domain.entities;

public class AuthResult {

    private final User user;
    private final String token;

    public AuthResult(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
