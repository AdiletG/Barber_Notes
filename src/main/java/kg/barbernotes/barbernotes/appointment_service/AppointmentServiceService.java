package kg.barbernotes.barbernotes.appointment_service;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
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
    public AppointmentServiceEntity getAppointmentByDate(UUID appointmentId) {
        return appointmentServiceRepository.findAppointmentServiceEntityByAppointmentEntity_Id(appointmentId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        ErrorCode.APPOINTMENT_SERVICE_NOT_FOUND,
                        "Appointment service with id " + appointmentId + " not found"
                ));

    }

    @Transactional(readOnly = true)
    public List<AppointmentServiceResponse> findByAppointmentId(UUID appointmentId) {
        return appointmentServiceRepository.findAppointmentServiceEntitiesByAppointmentEntity_Id(appointmentId)
                .stream()
                .map(appointmentServiceMapper::toResponse)
                .toList();
    }
}