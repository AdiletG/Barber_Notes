package kg.barbernotes.barbernotes.customer;
import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceService;
import kg.barbernotes.barbernotes.appointment_service.dto.AppointmentServiceResponse;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.customer.dto.CustomerResponse;
import kg.barbernotes.barbernotes.customer.dto.CustomerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerMeController {
    private final CustomerService customerService;
    private final AppointmentBookingService appointmentBookingService;
    private final AppointmentServiceService appointmentServiceService;

    @GetMapping
    public CustomerResponse getMe() {
        return customerService.findById();
    }

    @GetMapping("/appointments")
    public PageResponse<AppointmentResponse> getAppointments(
            @PageableDefault(
                    sort = {"appointmentDate", "startTime", "appointmentNumber"},
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
       return PageResponse.of(appointmentBookingService.findAllForCustomer(
                currentCustomer().getId(), status, dateFrom, dateTo, pageable
        ));
    }

    @GetMapping("/appointments/{appointmentId}")
    public AppointmentResponse getAppointmentById(@PathVariable UUID appointmentId) {
        return appointmentBookingService.findOwnById(currentCustomer().getId(), appointmentId);
    }

    @GetMapping("appointments/{appointmentId}/services")
    public List<AppointmentServiceResponse> getServices(@PathVariable UUID appointmentId) {
        AppointmentResponse appointmentResponse =
                appointmentBookingService.findOwnById(currentCustomer().getId(), appointmentId);

        return appointmentServiceService.findByAppointmentId(appointmentResponse.getId());
    }

    @PutMapping
    public CustomerResponse updateMe(
            @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(
                currentCustomer().getId(),
                request
        );
    }

    @PostMapping("appointments/{appointmentId}/cancel")
    public AppointmentResponse cancelAppointment(@PathVariable UUID appointmentId) {
       return appointmentBookingService.cancelByCustomer(currentCustomer().getId(), appointmentId);
    }


    private CustomerEntity currentCustomer(){
        return customerService.getCurrentCustomer();
    }
}