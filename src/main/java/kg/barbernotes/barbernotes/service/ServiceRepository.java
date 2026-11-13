package kg.barbernotes.barbernotes.service;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    @Query("""
    SELECT s
    FROM ServiceEntity s
    WHERE (:status IS NULL OR s.status = :status)
        AND (:categoryId IS NULL OR s.serviceCategoryEntity.id = :categoryId)

        AND (
            (:barberId IS NULL AND :branchId IS NULL)

            OR (
                s.status = :activeStatus
                AND s.serviceCategoryEntity.status = :activeStatus

                AND EXISTS (
                    SELECT bs.id
                    FROM BarberServiceEntity bs
                    WHERE bs.serviceEntity.id = s.id
                        AND bs.status = :activeStatus
                        AND bs.barberEntity.status = :activeStatus
                        AND bs.barberEntity.branchEntity.status = :activeStatus

                        AND (:barberId IS NULL OR bs.barberEntity.id = :barberId)
                        AND (:branchId IS NULL OR bs.barberEntity.branchEntity.id = :branchId)
                )
            )
        )
    """)
    Page<ServiceEntity> findAllFilters(
            @Param("status") Status status,
            @Param("categoryId") UUID categoryId,
            @Param("branchId") UUID branchId,
            @Param("barberId") UUID barberId,
            @Param("activeStatus") Status activeStatus,
            Pageable pageable
    );

    List<ServiceEntity> findByServiceCategoryEntity_IdAndStatus(UUID categoryId, Status status);

    boolean existsByName(String name);
}
