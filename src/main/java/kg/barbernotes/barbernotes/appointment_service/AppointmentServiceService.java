package kg.barbernotes.barbernotes.appointment_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceService {
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final AppointmentServiceMapper appointmentServiceMapper;

    @Transactional(readOnly = true)
    public List<AppointmentServiceResponse> findByAppointmentId(UUID appointmentId) {
        return appointmentServiceRepository.findAppointmentServiceEntitiesByAppointmentEntity_Id(appointmentId)
                .stream()
                .map(appointmentServiceMapper::toResponse)
                .toList();
    }
}