package kg.barbernotes.barbernotes.common.security.jwt;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import kg.barbernotes.barbernotes.common.exceptions.TokenReuseDetectedException;
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

    @Transactional(noRollbackFor = TokenReuseDetectedException.class)
    public RotatedRefreshToken rotate(String oldRawToken){

        String tokenHash = jwtService.hashToken(oldRawToken);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        int updated = refreshTokenRepository.revokeIfActive(tokenHash, now);

        RefreshTokenEntity findTokenHash = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException(
                        ErrorCode.INVALID_TOKEN,
                        "Такого токена не существует"
                ));

        if(updated == 1){

            IssuedRefreshToken newRowToken = issue(findTokenHash.getSubjectId(), findTokenHash.getSubjectType());
            findTokenHash.setReplacedById(newRowToken.entityId());

            return new RotatedRefreshToken(newRowToken.rawToken(), findTokenHash.getSubjectId(), findTokenHash.getSubjectType());

        }


            if(findTokenHash.getRevokedAt() != null){
                revokeAllForSubject(findTokenHash.getSubjectId(),  findTokenHash.getSubjectType());
                throw new TokenReuseDetectedException(
                        ErrorCode.INVALID_TOKEN,
                        "Ваша сессия скомпрометирована, войдите заново"
                );
            }


                throw new InvalidTokenException(
                        ErrorCode.TOKEN_EXPIRED,
                        "Время жизни токена истекло"
                );
    }

    @Transactional
    public void revokeByRawToken(String rawToken) {
        String tokenHash = jwtService.hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(entity -> revokeAllForSubject(entity.getSubjectId(), entity.getSubjectType()));
    }

    @Transactional
    public void revokeAllForSubject(UUID subjectId, SubjectType subjectType) {
        List<RefreshTokenEntity> list =
                refreshTokenRepository.findAllBySubjectIdAndSubjectTypeAndRevokedAtIsNull(subjectId, subjectType);
        for (RefreshTokenEntity refreshTokenEntity : list) {
            refreshTokenEntity.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        refreshTokenRepository.saveAll(list);
    }

    public record IssuedRefreshToken(String rawToken, UUID entityId) {}
    public record RotatedRefreshToken(String rawRefreshToken, UUID subjectId, SubjectType subjectType) {}


}