package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.common.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity>findByPhoneNumber(String phoneNumber);
    Page<CustomerEntity> findByStatus(Status status, Pageable pageable);
}
