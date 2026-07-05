package video.to.image.management_ms.infra.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-characters";

    private final JwtTokenService jwtTokenService = new JwtTokenService(SECRET);

    @Test
    void shouldValidateTokenWithUserIdClaim() {
        UUID userId = UUID.randomUUID();
        String token = Jwts.builder()
                .claim("userId", userId.toString())
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        AuthenticatedUser authenticatedUser = jwtTokenService.validate(token);

        assertThat(authenticatedUser.userId()).isEqualTo(userId);
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThatThrownBy(() -> jwtTokenService.validate("invalid-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldRejectTokenWithoutUserIdentifier() {
        String token = Jwts.builder()
                .claim("email", "user@email.com")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtTokenService.validate(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token JWT sem id do usuario");
    }
}
