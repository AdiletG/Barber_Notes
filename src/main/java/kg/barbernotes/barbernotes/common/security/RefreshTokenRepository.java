package kg.barbernotes.barbernotes.common.security;

import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String token);
    List<RefreshTokenEntity>findAllBySubjectIdAndRevokedAtIsNull(UUID subjectId);
}
