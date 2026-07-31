package kg.barbernotes.barbernotes.service_category;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.common.AuditableEntity;
import kg.barbernotes.barbernotes.common.Status;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "service_category")
public class ServiceCategoryEntity extends AuditableEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @OneToMany(mappedBy = "serviceCategoryEntity")
    private List<ServiceEntity> serviceEntities;
}