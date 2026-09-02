package kg.barbernotes.barbernotes.customer;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentResponse;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final AppointmentBookingService appointmentBookingService;

    @GetMapping("/{customerId}/appointments")
    @PreAuthorize("hasAnyRole('BARBER', 'BRANCH_ADMIN', 'SUPER_ADMIN')")
    public PageResponse<AppointmentResponse> getCustomerAppointments(
            @PathVariable UUID customerId, Pageable pageable) {
        return PageResponse.of(appointmentBookingService.findALLFromFilters(
                null, null, null, customerId, null, null, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public PageResponse<CustomerResponse> getAllCustomers(
            Pageable pageable,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String phoneNumber) {
        if(status == null && phoneNumber == null) {
            return PageResponse.of(customerService.findAll(pageable));
        }else if(phoneNumber == null) {
            return PageResponse.of(customerService.findByStatus(status, pageable));
        }else {
            return PageResponse.of(customerService.findByPhoneNumber(phoneNumber, pageable));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public CustomerResponse getCustomerByPhone(@PathVariable UUID id) {
        return customerService.findById(id);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public CustomerResponse update(
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest request
    ){
        return customerService.update(customerId, request);
    }

    @PutMapping("/{customerId}/status")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public CustomerResponse updateStatus(
            @PathVariable UUID customerId,
            @Valid @RequestBody StatusUpdateRequest request
    ){
        return customerService.updateStatus(customerId, request);
    }
}