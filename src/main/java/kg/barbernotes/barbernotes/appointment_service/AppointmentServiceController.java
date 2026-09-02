package kg.barbernotes.barbernotes.appointment_service;

import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments/{id}/services")
@RequiredArgsConstructor
public class AppointmentServiceController {
    private final AppointmentServiceService appointmentServiceService;
    private final AppointmentBookingService appointmentBookingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('BARBER', 'BRANCH_ADMIN', 'SUPER_ADMIN')")
    public List<AppointmentServiceResponse> findAllAppointments(@PathVariable UUID id) {
        if(!appointmentBookingService.existsById(id)) {
            throw new EntityNotFoundException(
                    ErrorCode.APPOINTMENT_NOT_FOUND,
                    "Запись с таким id не существует"
            );
        }
        return appointmentServiceService.findByAppointmentId(id);
    }
}