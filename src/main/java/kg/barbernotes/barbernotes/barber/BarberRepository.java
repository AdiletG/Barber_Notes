package kg.barbernotes.barbernotes.barber;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BarberRepository extends JpaRepository<BarberEntity, UUID> {

    @Query("""
    SELECT b
    FROM BarberEntity b
    WHERE (:branchId IS NULL OR b.branchEntity.id = :branchId)
        AND (:status IS NULL OR b.status = :status)

        AND b.status = :activeStatus
        AND b.branchEntity.status = :activeStatus

        AND (
            SELECT COUNT(DISTINCT bs.serviceEntity.id)
            FROM BarberServiceEntity bs
            WHERE bs.barberEntity = b
                AND bs.status = :activeStatus
                AND bs.serviceEntity.id IN :serviceIds
                AND bs.serviceEntity.status = :activeStatus
                AND bs.serviceEntity.serviceCategoryEntity.status = :activeStatus
        ) = :serviceCount
    """)
    Page<BarberEntity> findAllWithServices(
            @Param("branchId") UUID branchId,
            @Param("status") Status status,
            @Param("serviceIds") List<UUID> serviceIds,
            @Param("serviceCount") long serviceCount,
            @Param("activeStatus") Status activeStatus,
            Pageable pageable
    );

    Page<BarberEntity> findByBranchEntity_IdAndStatus(UUID branchEntityId, Status status,  Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    List<BarberEntity> findByBranchEntity_IdAndStatus(UUID branchId, Status status);
}
