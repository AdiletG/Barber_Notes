package kg.barbernotes.barbernotes.service_category;

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
@RequestMapping("/api/v1/service-categories")
@RequiredArgsConstructor
public class ServiceCategoryController {
    private final ServiceCategoryService serviceCategoryService;

    @GetMapping
    public PageResponse<ServiceCategoryResponse> getAllCategories(
            Pageable pageable, @RequestParam(required = false)Status status) {
        if(status == null) {
            return PageResponse.of(serviceCategoryService.getALLCategories(pageable));
        }
        return PageResponse.of(serviceCategoryService.findAllByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    public ServiceCategoryResponse getCategoryById(@PathVariable UUID id) {
        return serviceCategoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceCategoryResponse create(@Valid @RequestBody ServiceCategoryCreateRequest request){
        return serviceCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceCategoryResponse update(
            @PathVariable UUID id, @Valid @RequestBody ServiceCategoryUpdateRequest request
    ){
        return serviceCategoryService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public ServiceCategoryResponse inactive(
            @PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request){
        return serviceCategoryService.updateStatus(id, request);
    }
}