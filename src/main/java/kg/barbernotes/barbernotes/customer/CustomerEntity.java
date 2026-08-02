package kg.barbernotes.barbernotes.customer;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.appointment.AppointmentEntity;
import kg.barbernotes.barbernotes.common.entity.AuditableEntity;
import kg.barbernotes.barbernotes.common.enums.Source;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "customer")

public class CustomerEntity extends AuditableEntity {

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "phone_number", length = 15,
            nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "created_from", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Source createdFrom;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "customerEntity")
    private List<AppointmentEntity> appointmentEntities;
}