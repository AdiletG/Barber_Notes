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
        SELECT s FROM ServiceEntity s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:categoryId IS NULL OR s.serviceCategoryEntity.id = :categoryId)
          AND (
              :branchId IS NULL
              OR (
                  s.status = kg.barbernotes.barbernotes.common.enums.Status.ACTIVE
                  AND s.serviceCategoryEntity.status = kg.barbernotes.barbernotes.common.enums.Status.ACTIVE
                  AND EXISTS (
                      SELECT bs.id
                      FROM BarberServiceEntity bs
                      WHERE bs.serviceEntity.id = s.id
                        AND bs.status = kg.barbernotes.barbernotes.common.enums.Status.ACTIVE
                        AND bs.barberEntity.status = kg.barbernotes.barbernotes.common.enums.Status.ACTIVE
                        AND bs.barberEntity.branchEntity.id = :branchId
                        AND bs.barberEntity.branchEntity.status = kg.barbernotes.barbernotes.common.enums.Status.ACTIVE
                  )
              )
          )
        """)
    Page<ServiceEntity> findAllFilters(
            @Param("status") Status status,
            @Param("categoryId") UUID categoryId,
            @Param("branchId") UUID branchId,
            Pageable pageable
    );



    boolean existsByServiceCategoryEntity_Id(UUID id);
    Page<ServiceEntity> findAllByStatus(Status status, Pageable pageable);
    Page<ServiceEntity> findByServiceCategoryEntity_Id(UUID categoryId, Pageable pageable);
    Page<ServiceEntity> findByServiceCategoryEntity_IdAndStatus(UUID categoryId, Status status, Pageable pageable);
    List<ServiceEntity> findByServiceCategoryEntity_IdAndStatus(UUID categoryId, Status status);

    boolean existsByName(String name);
}
