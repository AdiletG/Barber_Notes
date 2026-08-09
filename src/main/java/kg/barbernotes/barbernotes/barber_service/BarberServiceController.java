package kg.barbernotes.barbernotes.barber_service;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers/{barberId}/services")
@RequiredArgsConstructor
public class BarberServiceController {
    private final BarberServiceService  barberServiceService;

    @GetMapping
    public List<BarberServiceResponse> getBarberServices(
            @PathVariable UUID barberId,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) Status status
    ){
        if(status == null && serviceId == null){
            return barberServiceService.findByBarberId(barberId);
        }else if(serviceId == null){
            return barberServiceService.getByBarberStatus(barberId, status);
        }else if(status == null){
            return barberServiceService.getByBarberIdServiceId(barberId, serviceId);
        }else {
            return barberServiceService.findByBarberIdAndServiceIdAndStatus(barberId, serviceId, status);

        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BarberServiceResponse create(
            @PathVariable UUID barberId,
            @Valid @RequestBody AddBarberServiceRequest request) {
        return barberServiceService.create(barberId, request);
    }

    @PutMapping("/{id}")
    public BarberServiceResponse updateStatus(
            @PathVariable UUID id,
            @PathVariable UUID barberId,
            @Valid @RequestBody StatusUpdateRequest request
    ){
        return barberServiceService.updateStatus(barberId, id, request);
    }
}