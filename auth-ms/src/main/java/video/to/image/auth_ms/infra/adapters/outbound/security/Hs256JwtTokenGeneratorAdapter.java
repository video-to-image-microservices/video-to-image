package video.to.image.auth_ms.infra.adapters.outbound.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import video.to.image.auth_ms.core.application.ports.out.TokenGeneratorOutputPort;
import video.to.image.auth_ms.core.domain.entities.User;
import video.to.image.auth_ms.infra.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class Hs256JwtTokenGeneratorAdapter implements TokenGeneratorOutputPort {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public Hs256JwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generate(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
