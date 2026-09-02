package kg.barbernotes.barbernotes.barber;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment.AppointmentEntity;
import kg.barbernotes.barbernotes.barber_service.BarberServiceEntity;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.staff_account.StaffAccountEntity;
import kg.barbernotes.barbernotes.work_shift.WorkShiftEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "barber")
public class BarberEntity extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id",  nullable = false)
    private BranchEntity branchEntity;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @OneToMany(mappedBy = "barberEntity")
    private List<BarberServiceEntity> barbersServices;

    @OneToMany(mappedBy = "barberEntity")
    private List<WorkShiftEntity> workShiftEntities;

    @OneToMany(mappedBy = "barberEntity")
    private List<AppointmentEntity> appointmentEntities;

    @OneToMany(mappedBy = "barberEntity")
    private List<StaffAccountEntity> staffAccountEntities;
}