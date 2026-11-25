package org.springboot.smartdining.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Cấp access token dạng JWT ký HS256. */
@Component
public class TokenService {

    public static final String ISSUER = "smartdining";
    public static final String SESSION_CLAIM = "sid";

    /** @param expiresInSeconds số giây còn hiệu lực */
    public record AccessToken(String value, long expiresInSeconds) {
    }

    private final JwtEncoder jwtEncoder;
    private final Duration accessTimeToLive;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${smartdining.security.access-ttl:15m}") Duration accessTimeToLive) {
        if (accessTimeToLive.toSeconds() < 1) {
            throw new IllegalStateException("smartdining.security.access-ttl phải ít nhất 1 giây");
        }
        this.jwtEncoder = jwtEncoder;
        this.accessTimeToLive = accessTimeToLive;
    }

    /** Access token không sống lâu hơn phiên của nó. */
    public AccessToken issue(Long employeeId, String sessionId, LocalDateTime sessionExpiresAt) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant sessionEnd = sessionExpiresAt.atZone(ZoneId.systemDefault()).toInstant();
        Instant expiresAt = now.plus(accessTimeToLive);
        if (expiresAt.isAfter(sessionEnd)) {
            expiresAt = sessionEnd;
        }
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(employeeId))
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(SESSION_CLAIM, sessionId)
                .build();
        String value = jwtEncoder
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new AccessToken(value, Duration.between(now, expiresAt).toSeconds());
    }
}
