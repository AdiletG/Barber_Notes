package kg.barbernotes.barbernotes.appointment_service;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment.AppointmentEntity;
import kg.barbernotes.barbernotes.common.BaseEntity;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(name = "appointment_service",
        uniqueConstraints = @UniqueConstraint(columnNames = {"appointment_id", "service_id"}))
public class AppointmentServiceEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id",  nullable = false)
    private AppointmentEntity appointmentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id",  nullable = false)
    private ServiceEntity serviceEntity;

    @Column(name = "service_name",   nullable = false)
    private String serviceName;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
}