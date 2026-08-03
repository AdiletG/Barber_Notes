package kg.barbernotes.barbernotes.branch;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment.AppointmentEntity;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "branch")
public class BranchEntity extends AuditableEntity {

    @Column(length = 100,  nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String address;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "workplace_count", nullable = false)
    private Integer workplaceCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status;

    @OneToMany(mappedBy = "branchEntity")
    private List<BarberEntity> barberEntities;

    @OneToMany(mappedBy = "branchEntity")
    private List<AppointmentEntity> appointmentEntities;
}