package kg.barbernotes.barbernotes.barber_service;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BarberServiceRepository extends JpaRepository<BarberServiceEntity, UUID> {

    Optional<BarberServiceEntity> findByBarberEntity_IdAndServiceEntity_Id(UUID barberId, UUID serviceEntityId);
    Page<BarberServiceEntity> findByBarberEntity_IdAndStatus(UUID barberId, Status status, Pageable pageable);
    Page<BarberServiceEntity> findByBarberEntity_Id(UUID barberId, Pageable pageable);
    List<BarberServiceEntity> findByBarberEntity_IdAndStatus(UUID barberId, Status status);
}
