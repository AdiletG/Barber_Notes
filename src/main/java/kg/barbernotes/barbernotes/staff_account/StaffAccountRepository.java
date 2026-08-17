package kg.barbernotes.barbernotes.staff_account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, UUID> {
    Boolean existsByPhoneNumber(String phoneNumber);

}
