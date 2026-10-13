package kg.barbernotes.barbernotes.common.security.jwt;

import kg.barbernotes.barbernotes.common.enums.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String token);

    @Modifying(clearAutomatically = true)
      @Query(""" 
                UPDATE RefreshTokenEntity t
                SET t.revokedAt = :now
                WHERE t.tokenHash = :hash AND t.revokedAt IS NULL AND t.expiresAt > :now""")
    int revokeIfActive(@Param("hash")  String hash, @Param("now") OffsetDateTime now);

    List<RefreshTokenEntity>findAllBySubjectIdAndSubjectTypeAndRevokedAtIsNull
            (UUID subjectId,  SubjectType subjectType);
}
