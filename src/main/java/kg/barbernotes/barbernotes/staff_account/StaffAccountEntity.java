package kg.barbernotes.barbernotes.staff_account;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "staff_account")
public class StaffAccountEntity extends AuditableEntity {

    @Column(name = "phone_number", length = 15,
            nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private StaffRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private BarberEntity barberEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private BranchEntity branchEntity;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @Column(name = "must_change_password",  nullable = false)
    private Boolean mustChangePassword;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "locked_until")
    OffsetDateTime  lockedUntil;

    @Column(name = "last_login_at")
    OffsetDateTime lastLoginAt;
}