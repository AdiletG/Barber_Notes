package kg.barbernotes.barbernotes.customer;
import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.customer.dto.CustomerResponse;
import kg.barbernotes.barbernotes.customer.dto.CustomerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerMeController {
    private final CustomerService customerService;
    private final AppointmentBookingService appointmentBookingService;

    @GetMapping
    public CustomerResponse getMe() {
        return customerService.findById(currenCustomer().getId());
    }

    @GetMapping("/appointments")
    public PageResponse<AppointmentResponse> getAppointments(
            @PageableDefault(
                    sort = {"appointmentDate", "startTime", "appointmentNumber"},
                    direction = Sort.Direction.ASC
            )
            Pageable pageable,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
       return PageResponse.of(appointmentBookingService.findAllForCustomer(
                currenCustomer().getId(), status, dateFrom, dateTo, pageable
        ));
    }

    @GetMapping("/appointments/{appointmentId}")
    public AppointmentResponse getAppointmentById(@PathVariable UUID appointmentId) {
        return appointmentBookingService.findOwnById(currenCustomer().getId(), appointmentId);
    }

    @PutMapping
    public CustomerResponse updateMe(
            @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(
                currenCustomer().getId(),
                request
        );
    }

    @PostMapping("appointments/{appointmentId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelAppointment(@PathVariable UUID appointmentId) {
        appointmentBookingService.cancelByCustomer(currenCustomer().getId(), appointmentId);
    }


    private CustomerEntity currenCustomer(){
        return customerService.getCurrentCustomer();
    }
}