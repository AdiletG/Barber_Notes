package kg.barbernotes.barbernotes.service;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceEntity;
import kg.barbernotes.barbernotes.barber_service.BarberServiceEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "service")
public class ServiceEntity extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",  nullable = false)
    private ServiceCategoryEntity serviceCategoryEntity;

    @Column(nullable = false)
    private String name;
    private String description;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @OneToMany(mappedBy = "serviceEntity")
    private List<BarberServiceEntity> barbersServices;

    @OneToMany(mappedBy = "serviceEntity")
    private List<AppointmentServiceEntity> appointmentServiceEntities;
}