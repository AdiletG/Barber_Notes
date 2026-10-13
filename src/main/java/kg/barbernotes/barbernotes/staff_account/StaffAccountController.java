package kg.barbernotes.barbernotes.staff_account;

import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateResponse;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff-accounts")
@RequiredArgsConstructor
public class StaffAccountController {
    private final StaffAccountService staffAccountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public StaffAccountCreateResponse create(
           @Valid @RequestBody StaffAccountCreateRequest request
    ) {
        return staffAccountService.create(request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public StaffAccountResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request
    ){
        return staffAccountService.statusUpdate(id, request);
    }

}