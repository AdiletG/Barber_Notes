package kg.barbernotes.barbernotes.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    @Query("""
        SELECT COUNT(a)
        FROM AppointmentEntity a
        WHERE a.customerEntity.id = :customerId
          AND a.status = :status
          AND (
              a.appointmentDate > :fromDate
              OR (
                  a.appointmentDate = :fromDate
                  AND a.startTime >= :fromTime
              )
          )
        """)
    long countCustomerFutureAppointments(
            @Param("customerId") UUID customerId,
            @Param("status") AppointmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("fromTime") LocalTime fromTime
    );

    @Query("""
        SELECT COUNT(a) > 0
        FROM AppointmentEntity a
        WHERE (:branchId IS NULL OR a.branchEntity.id = :branchId)
          AND (:status IS NULL OR a.status = :status)
          AND (
              a.appointmentDate > :fromDate
              OR (
                  a.appointmentDate = :fromDate
                  AND a.startTime >= :fromTime
              )
          )
        """)
    boolean hasUpcomingBranch(
            @Param("branchId") UUID branchId,
            @Param("status") AppointmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("fromTime") LocalTime fromTime
    );

    @Query("""
        SELECT COUNT(a) > 0
        FROM AppointmentEntity a
        WHERE (:barberId IS NULL OR a.barberEntity.id = :barberId)
          AND (:status IS NULL OR a.status = :status)
          AND (
              a.appointmentDate > :fromDate
              OR (
                  a.appointmentDate = :fromDate
                  AND a.startTime >= :fromTime
              )
          )
        """)
    boolean hasUpcoming(
            @Param("barberId") UUID barberId,
            @Param("status") AppointmentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("fromTime") LocalTime fromTime
    );
    Page<AppointmentEntity> findAllByStatus(AppointmentStatus status, Pageable pageable);

    Boolean existsByBarberEntity_IdAndAppointmentDateAndStatus(
            UUID barberEntity, LocalDate date, AppointmentStatus status);

    List<AppointmentEntity> findAppointmentEntitiesByBarberEntity_IdAndAppointmentDate(
            UUID barberEntity, LocalDate appointmentDate);

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
