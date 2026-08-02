package kg.barbernotes.barbernotes.barber_service;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "barber_service",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barber_id", "service_id"}))
public class BarberServiceEntity extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id",  nullable = false)
    private BarberEntity barberEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id",  nullable = false)
    private ServiceEntity serviceEntity;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;
}