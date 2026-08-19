package kg.barbernotes.barbernotes.common.security.jwt;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import kg.barbernotes.barbernotes.common.exceptions.TokenReuseDetectedException;
import kg.barbernotes.barbernotes.common.security.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtService  jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public IssuedRefreshToken issue(UUID subjectId, SubjectType subjectType){
        String rawToken = jwtService.generateRefreshToken(subjectId, subjectType);
        String hashToken = jwtService.hashToken(rawToken);

        RefreshTokenEntity  refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setSubjectType(subjectType);
        refreshTokenEntity.setSubjectId(subjectId);
        refreshTokenEntity.setTokenHash(hashToken);
        refreshTokenEntity.setExpiresAt(OffsetDateTime.ofInstant(
                jwtService.calculateRefreshExpiry(subjectType), ZoneOffset.UTC));

        RefreshTokenEntity saved = refreshTokenRepository.save(refreshTokenEntity);

        return new IssuedRefreshToken(rawToken, saved.getId());

    }

    @Transactional
    public String rotate(String oldRawToken){

        String tokenHash = jwtService.hashToken(oldRawToken);
        RefreshTokenEntity findTokenHash = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        ErrorCode.INVALID_TOKEN,
                        "Такого токена не существует"
                ));

        if(findTokenHash.getRevokedAt() != null){
            revokeAllForSubject(findTokenHash.getSubjectId());
            throw new TokenReuseDetectedException(
                    ErrorCode.INVALID_TOKEN,
                    "Ваша сессия скомпрометирована, войдите заново"
            );
        }

        if(findTokenHash.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))){
            throw new InvalidTokenException(
                    ErrorCode.TOKEN_EXPIRED,
                    "Время жизни токена истекло"
            );
        }

        IssuedRefreshToken newRowToken = issue(findTokenHash.getSubjectId(), findTokenHash.getSubjectType());
        findTokenHash.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        findTokenHash.setReplacedById(newRowToken.entityId());
        refreshTokenRepository.save(findTokenHash);

        return newRowToken.rawToken();
    }

    private void revokeAllForSubject(UUID subjectId) {
        List<RefreshTokenEntity> list = refreshTokenRepository.findAllBySubjectIdAndRevokedAtIsNull(subjectId);
        for (RefreshTokenEntity refreshTokenEntity : list) {
            refreshTokenEntity.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        refreshTokenRepository.saveAll(list);
    }

    public record IssuedRefreshToken(String rawToken, UUID entityId) {}

}