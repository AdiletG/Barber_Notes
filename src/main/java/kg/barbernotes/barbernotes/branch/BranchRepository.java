package kg.barbernotes.barbernotes.branch;

import kg.barbernotes.barbernotes.common.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {

    Page<BranchEntity> findAllByStatus(Status status, Pageable pageable);
    Optional<BranchEntity> findByAddress(String address);
}
