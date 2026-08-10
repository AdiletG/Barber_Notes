package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Page<CustomerEntity>findByPhoneNumber(String phoneNumber, Pageable pageable);
    Page<CustomerEntity> findByStatus(Status status, Pageable pageable);
}
