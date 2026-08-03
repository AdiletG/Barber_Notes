package kg.barbernotes.barbernotes.appointment;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceEntity;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Source;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "appointment")

public class AppointmentEntity extends AuditableEntity {

    @Column(name = "appointment_number", insertable = false,
            updatable = false, nullable = false, unique = true)
    private Long appointmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberEntity barberEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;

    @Column(name = "appointment_date",  nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "total_duration_minutes",  nullable = false)
    private Integer totalDurationMinutes;

    @Column(name = "total_price", precision = 10, scale = 2,  nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Source source;

    @OneToMany(mappedBy = "appointmentEntity")
    private List<AppointmentServiceEntity> appointmentServiceEntities;

}