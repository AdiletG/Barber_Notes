package kg.barbernotes.barbernotes.staff_account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, UUID> {

    @Modifying(clearAutomatically = true,  flushAutomatically = true)
    @Query("""
    update StaffAccountEntity s
        set s.failedLoginAttempts = 0,
            s.lockedUntil = null,
            s.lastLoginAt = :now,
            s.version = s.version + 1,
            s.updatedAt = :now
        where s.id = :id
    """)
    int successfulAttempt(UUID id, OffsetDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update StaffAccountEntity s 
        set s.failedLoginAttempts = case
            when s.failedLoginAttempts + 1 >= :limit
            then 0
            else s.failedLoginAttempts + 1
        end,
        s.lockedUntil = case
            when s.failedLoginAttempts + 1 >= :limit
            then :lockTime
            else s.lockedUntil
        end,
            s.version = s.version + 1,
            s.updatedAt = :now
        where s.id = :id
    """)
    int unsuccessfulAttempt(UUID id, int limit, OffsetDateTime lockTime, OffsetDateTime now);

    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByBarberEntity_Id(UUID id);
    Optional<StaffAccountEntity> findByPhoneNumber(String phoneNumber);
}
