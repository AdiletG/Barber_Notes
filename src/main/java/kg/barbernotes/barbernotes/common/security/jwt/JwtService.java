package kg.barbernotes.barbernotes.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtService {
    private final JwtProperties  jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public Instant calculateRefreshExpiry(SubjectType subjectType) {
        Duration ttl = subjectType == SubjectType.CUSTOMER
                ? Duration.ofMinutes(jwtProperties.customer().refreshTtlMinutes())
                : Duration.ofMinutes(jwtProperties.staff().refreshTtlMinutes());
        return Instant.now().plus(ttl);
    }

    public String generateCustomerAccessToken(UUID subjectId, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("role", "CUSTOMER");
        Duration ttl = Duration.ofMinutes(jwtProperties.customer().accessTtlMinutes());

        return buildToken(subjectId.toString(), claims, "access", ttl);
    }

    public String generateStaffAccessToken(UUID subjectId, StaffRole role, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("role", role.name());
        Duration ttl = Duration.ofMinutes(jwtProperties.staff().accessTtlMinutes());

        return buildToken(subjectId.toString(), claims, "access", ttl);
    }

    public String generateRefreshToken(UUID subjectId, SubjectType subjectType) {
        Duration ttl = subjectType == SubjectType.CUSTOMER
                ? Duration.ofMinutes(jwtProperties.customer().refreshTtlMinutes())
                : Duration.ofMinutes(jwtProperties.staff().refreshTtlMinutes());

        return buildToken(subjectId.toString(), Map.of(), "refresh", ttl);
    }

    private String buildToken(String subject, Map<String, Object> extraClaims, String tokenType,Duration ttl) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ttl);

        return Jwts.builder()
                .subject(subject)
                .claims(extraClaims)
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (ExpiredJwtException e){
            throw new InvalidTokenException(ErrorCode.TOKEN_EXPIRED, "Токен истёк");
        }catch (JwtException e){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
        }
    }

    public String hashToken(String token) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }
}