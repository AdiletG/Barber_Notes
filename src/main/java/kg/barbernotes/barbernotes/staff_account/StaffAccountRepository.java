package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, UUID> {
    Boolean existsByPhoneNumber(String phoneNumber);
    Boolean existsByBarberEntity_Id(UUID id);
    Optional<StaffAccountEntity> findByPhoneNumber(String phoneNumber);
}
