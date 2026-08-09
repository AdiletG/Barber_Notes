package kg.barbernotes.barbernotes.service;

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
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceService serviceService;

    @GetMapping
    public PageResponse<ServiceResponse> findAll(
            Pageable pageable,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UUID categoryId
            ){
        if(status == null && categoryId == null){
            return PageResponse.of(serviceService.findAll(pageable));
        }else if(status == null){
            return PageResponse.of(serviceService.findAllByCategoryId(categoryId, pageable));
        }else if(categoryId == null){
            return PageResponse.of(serviceService.findAllByStatus(status, pageable));
        }else {
            return PageResponse.of(serviceService.findPageCategoryAndStatus(categoryId, status, pageable));
        }
    }

    @GetMapping("/{id}")
    public ServiceResponse findOne(@PathVariable UUID id) {
        return serviceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create (@Valid @RequestBody ServiceCreateRequest request){
        return serviceService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceUpdateRequest request){
        return serviceService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public ServiceResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request){
        return serviceService.updateStatus(id, request);
    }
}