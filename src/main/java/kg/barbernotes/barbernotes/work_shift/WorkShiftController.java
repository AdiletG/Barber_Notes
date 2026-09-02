package kg.barbernotes.barbernotes.work_shift;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkShiftController {
    private  final WorkShiftService workShiftService;

    @GetMapping("/api/v1/barbers/{barberId}/shifts")
    public List<WorkShiftResponse> getWorkShifts(
            @PathVariable UUID barberId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
            ){
        if(dateFrom == null && dateTo == null){
            return workShiftService.findByWorkShift(barberId);
        }else {
            return workShiftService.findByWorkDateBetween(barberId, dateFrom, dateTo);
        }
    }

    @PostMapping("/api/v1/barbers/{barberId}/shifts")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public WorkShiftResponse createWorkShift(
            @PathVariable UUID barberId,
            @Valid @RequestBody WorkShiftCreateRequest request
    ){
        return workShiftService.create(barberId, request);
    }

    @PutMapping("/api/v1/shifts/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public WorkShiftResponse updateWorkShift(
            @PathVariable UUID id,
            @Valid @RequestBody WorkShiftUpdateRequest request
    ){
        return workShiftService.update(id, request);
    }

    @DeleteMapping("/api/v1/shifts/{id}")
    @PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'SUPER_ADMIN')")
    public void deleteWorkShift(@PathVariable UUID id){
        workShiftService.delete(id);
    }
}