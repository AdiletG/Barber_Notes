package kg.barbernotes.barbernotes.service;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.service.dto.ServiceCreateRequest;
import kg.barbernotes.barbernotes.service.dto.ServiceResponse;
import kg.barbernotes.barbernotes.service.dto.ServiceUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping
    public PageResponse<ServiceResponse> findAll(
            @PageableDefault(
                    sort = {"name"},
                    direction = Sort.Direction.ASC
            )
            Pageable pageable,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID barberId
            ){
        return PageResponse.of(serviceService.findAllWithFilters(status, categoryId, branchId, barberId, pageable));
    }

    @GetMapping("/{id}")
    public ServiceResponse findOne(@PathVariable UUID id) {
        return serviceService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create (@Valid @RequestBody ServiceCreateRequest request){
        return serviceService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceUpdateRequest request){
        return serviceService.update(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ServiceResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request){
        return serviceService.updateStatus(id, request);
    }
}