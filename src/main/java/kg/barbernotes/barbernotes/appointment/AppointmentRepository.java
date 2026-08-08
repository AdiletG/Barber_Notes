package kg.barbernotes.barbernotes.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Page<AppointmentEntity> findAllByBranchEntity_Id(UUID branchId, Pageable pageable);
    Page<AppointmentEntity> findAllByBarberEntity_Id(UUID barberId, Pageable pageable);
    Page<AppointmentEntity> findAllByCustomerEntity_Id(UUID customerId, Pageable pageable);

    @Query("""
            SELECT a FROM AppointmentEntity a \
            WHERE (:status IS NULL OR a.status = :status) \
            AND (:branchId IS NULL OR a.branchEntity.id = :branchId) \
            AND (:barberId IS NULL OR a.barberEntity.id = :barberId) \
            AND (:customerId IS NULL OR a.customerEntity.id = :customerId) \
            AND (CAST(:fromDate AS date) IS NULL OR a.appointmentDate >= :fromDate) \
            AND (CAST(:toDate AS date) IS NULL OR a.appointmentDate <= :toDate)""")
    Page<AppointmentEntity> findAllWithFilters(
            @Param("status") AppointmentStatus status,
            @Param("branchId") UUID branchId,
            @Param("barberId") UUID barberId,
            @Param("customerId") UUID customerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

}
