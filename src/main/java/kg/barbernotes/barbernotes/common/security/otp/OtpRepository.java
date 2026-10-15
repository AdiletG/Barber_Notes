package kg.barbernotes.barbernotes.common.security.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE OtpEntity o SET o.attempts = o.attempts + 1
    WHERE o.id = :id
      AND o.consumedAt IS NULL
      AND o.expiresAt > :now
      AND o.attempts < :maxAttempts
    """)
    int reserveAttempt(UUID id, OffsetDateTime now, int maxAttempts);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE OtpEntity o SET o.consumedAt = :now
    WHERE o.id = :id AND o.consumedAt IS NULL
    """)
    int consumeIfActive(UUID id, OffsetDateTime now);
    Optional<OtpEntity> findFirstByCustomerEntity_IdOrderByCreatedAtDesc(UUID customerId);
}
