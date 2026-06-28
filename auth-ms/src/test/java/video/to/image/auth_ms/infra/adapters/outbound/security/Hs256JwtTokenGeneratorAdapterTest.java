package video.to.image.auth_ms.infra.adapters.outbound.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Hs256JwtTokenGeneratorAdapterTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-characters";

    private Hs256JwtTokenGeneratorAdapter tokenGenerator;
    private SecretKey secretKey;
    private User user;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setExpirationMs(3600000);

        tokenGenerator = new Hs256JwtTokenGeneratorAdapter(jwtProperties);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        UUID userId = UUID.randomUUID();
        user = new User(userId, "João", "joao@email.com", "hashed-password");
    }

    @Test
    void generate_shouldIncludeExpectedClaims() {
        String token = tokenGenerator.generate(user);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getId().toString(), claims.get("userId", String.class));
        assertEquals(user.getEmail(), claims.get("email", String.class));
        assertEquals(user.getName(), claims.get("name", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }
}
