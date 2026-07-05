package video.to.image.management_ms.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;

    public JwtTokenService(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(resolveSecret(jwtSecret));
    }

    public AuthenticatedUser validate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticatedUser(resolveUserId(claims));
    }

    private byte[] resolveSecret(String secret) {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private UUID resolveUserId(Claims claims) {
        Object userIdClaim = claims.get("userId");
        if (userIdClaim == null) {
            userIdClaim = claims.get("id");
        }
        if (userIdClaim == null) {
            userIdClaim = claims.getSubject();
        }
        if (userIdClaim == null) {
            throw new IllegalArgumentException("Token JWT sem id do usuario");
        }
        return UUID.fromString(userIdClaim.toString());
    }
}
