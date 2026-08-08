package kg.barbernotes.barbernotes.barber;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BarberRepository extends JpaRepository<BarberEntity, UUID> {

    Page<BarberEntity> findAllByStatus(Status status, Pageable pageable);
    Page<BarberEntity> findByBranchEntity_Id(UUID branchEntityId,  Pageable pageable);
    Page<BarberEntity> findByBranchEntity_IdAndStatus(UUID branchEntityId, Status status,  Pageable pageable);
    List<BarberEntity> findByBranchEntity_IdAndStatus(UUID branchEntityId, Status status);

    List<BarberEntity> findAllByBranchEntity_Id(UUID branchEntityId);
    boolean existsByPhoneNumber(String phoneNumber);
}
