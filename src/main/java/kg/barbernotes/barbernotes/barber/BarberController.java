package kg.barbernotes.barbernotes.barber;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.barber.dto.BarberCreateRequest;
import kg.barbernotes.barbernotes.barber.dto.BarberResponse;
import kg.barbernotes.barbernotes.barber.dto.BarberUpdateRequest;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
public class BarberController {
    private final BarberService barberService;

    @GetMapping
    public PageResponse<BarberResponse> findAllWithFilters(
            @PageableDefault(
                    direction = Sort.Direction.ASC
            )
            Pageable pageable,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) List<UUID> serviceIds
            ){
       return PageResponse.of(barberService.findAllWithFilters(branchId, status, serviceIds, pageable));
    }

    @GetMapping("/{id}")
    public BarberResponse findById(@PathVariable UUID id){
        return barberService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BarberResponse create(@Valid @RequestBody BarberCreateRequest request){
        return barberService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public BarberResponse update(@PathVariable UUID id,@Valid @RequestBody BarberUpdateRequest request){
        return barberService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public BarberResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request){
        return barberService.updateStatus(id, request);
    }
}