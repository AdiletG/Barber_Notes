package kg.barbernotes.barbernotes.staff_account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, UUID> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByBarberEntity_Id(UUID id);
    Optional<StaffAccountEntity> findByPhoneNumber(String phoneNumber);
}
