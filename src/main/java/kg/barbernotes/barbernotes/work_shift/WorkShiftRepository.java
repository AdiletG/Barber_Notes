package kg.barbernotes.barbernotes.work_shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkShiftRepository extends JpaRepository<WorkShiftEntity, UUID> {

    List<WorkShiftEntity> findByBarberEntity_Id(UUID barberId);
    List<WorkShiftEntity> findByBarberEntity_IdAndWorkDateBetween(UUID barberId, LocalDate dateFrom, LocalDate dateTo);
    Optional<WorkShiftEntity> findByBarberEntity_IdAndWorkDate(UUID barberId, LocalDate workDate);
}
