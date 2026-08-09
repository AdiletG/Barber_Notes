package kg.barbernotes.barbernotes.branch;

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
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping
    public PageResponse<BranchResponse> getBranches(
            Pageable pageable, @RequestParam(required = false) Status status) {

        if(status == null) {
            return PageResponse.of(branchService.getAllBranches(pageable));
        }
        return PageResponse.of(branchService.findAllByStatus(status, pageable));
    }

    @GetMapping("/{id}")
    public BranchResponse getBranch(@PathVariable UUID id) {
        return branchService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@Valid @RequestBody BranchCreateRequest request){
        return branchService.create(request);
    }

    @PutMapping("/{id}")
    public BranchResponse update(@PathVariable UUID id, @Valid @RequestBody BranchUpdateRequest request){
       return branchService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public BranchResponse inactivateBranch(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
       return branchService.inactive(id, request);
    }
}