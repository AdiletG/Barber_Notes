package kg.barbernotes.barbernotes.service_category;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, UUID> {

    Page<ServiceCategoryEntity> findAllByStatus(Status status, Pageable pageable);
    Optional<ServiceCategoryEntity> findByName(String name);

}
