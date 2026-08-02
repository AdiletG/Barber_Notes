package kg.barbernotes.barbernotes.appointment_service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentServiceRepository extends JpaRepository<AppointmentServiceEntity, UUID> {

    List<AppointmentServiceEntity> findAppointmentServiceEntitiesByAppointmentEntity_Id(UUID id);}
