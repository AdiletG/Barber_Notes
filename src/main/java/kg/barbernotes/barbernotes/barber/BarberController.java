package kg.barbernotes.barbernotes.barber;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.dto.PageResponse;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
public class BarberController {
    private final BarberService barberService;

    @GetMapping
    public PageResponse<BarberResponse> findAll(
            Pageable pageable,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) Status status
    ){
        if(status == null && branchId == null){
            return PageResponse.of(barberService.findAll(pageable));
        }else if(branchId == null){
            return PageResponse.of(barberService.findAllByStatus(status, pageable));
        } else if (status == null) {
            return PageResponse.of(barberService.findByBranchId(branchId, pageable));
        }else {
            return PageResponse.of(barberService.findByBranchIdAndStatus(branchId, status, pageable));
        }
    }

    @GetMapping("/{id}")
    public BarberResponse findById(@PathVariable UUID id){
        return barberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BarberResponse create(@Valid @RequestBody BarberCreateRequest request){
        return barberService.create(request);
    }

    @PutMapping("/{id}")
    public BarberResponse update(@PathVariable UUID id,@Valid @RequestBody BarberUpdateRequest request){
        return barberService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public BarberResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request){
        return barberService.updateStatus(id, request);
    }
}