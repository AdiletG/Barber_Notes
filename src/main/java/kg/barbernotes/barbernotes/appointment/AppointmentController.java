package kg.barbernotes.barbernotes.appointment;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentBookingService bookingService;
    private final CreateAppointmentUseCase createAppointmentUseCase;

    @GetMapping
    public PageResponse<AppointmentResponse> getAllAppointment(
            Pageable pageable,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID barberId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
            ){
            return PageResponse.of(bookingService.findALLFromFilters(
                    status, branchId, barberId, customerId, dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable UUID id){
        return bookingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request
    ){
        return createAppointmentUseCase.execute(request);

    }

    @PutMapping("/{id}/status")
    public AppointmentResponse updateAppointmentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request
    ){
        return bookingService.statusUpdate(id, request);
    }

}