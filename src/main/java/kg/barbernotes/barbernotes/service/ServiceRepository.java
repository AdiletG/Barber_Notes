package kg.barbernotes.barbernotes.service;

import kg.barbernotes.barbernotes.common.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Page<ServiceEntity> findAllByStatus(Status status, Pageable pageable);
    Page<ServiceEntity> findByServiceCategoryEntity_Id(UUID categoryId, Pageable pageable);
    Page<ServiceEntity> findByServiceCategoryEntity_IdAndStatus(UUID categoryId, Status status, Pageable pageable);
    List<ServiceEntity> findByServiceCategoryEntity_IdAndStatus(UUID categoryId, Status status);

}
