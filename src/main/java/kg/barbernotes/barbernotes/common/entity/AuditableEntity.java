package kg.barbernotes.barbernotes.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;


@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {
    @LastModifiedDate
    @Column(nullable = false)
    OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    Integer version;
}