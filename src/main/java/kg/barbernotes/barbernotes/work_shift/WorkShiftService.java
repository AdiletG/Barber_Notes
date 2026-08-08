package kg.barbernotes.barbernotes.work_shift;

import kg.barbernotes.barbernotes.barber.*;
import kg.barbernotes.barbernotes.branch.BranchResponse;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class WorkShiftService {
    private final WorkShiftRepository workShiftRepository;
    private final WorkShiftMapper workShiftMapper;
    private final BranchService branchService;
    private final BarberService barberService;


    @Transactional
    public void update(UUID workShiftId, WorkShiftUpdateRequest request) {
        WorkShiftEntity workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "Графика на эту дату не существует"
                ));

        workShiftMapper.updateEntity(request, workShift);
        workShiftRepository.save(workShift);
    }

    @Transactional
    public void create(UUID barberId, WorkShiftCreateRequest request){
        BarberEntity barber = barberService.getById(barberId);
        BranchResponse branch = branchService.findById(barber.getBranchEntity().getId());

        if(request.getWorkDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Дата графика работы не может быть создана в прошлом"
            );
        }

        if(request.getStartTime().isBefore(branch.getOpenTime()) ||
                request.getEndTime().isAfter(branch.getCloseTime())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала и конец рабочего дня должен быть в пределах работы филиала"
            );
        }

        WorkShiftEntity workShift = workShiftMapper.toEntity(request);

        workShift.setBarberEntity(barber);

        workShiftRepository.save(workShift);
    }



    @Transactional(readOnly = true)
    public WorkShiftResponse findByBarberIdWorkDate(UUID barberId, LocalDate date) {
        return workShiftRepository.findByBarberEntity_IdAndWorkDate(barberId, date)
                .map(workShiftMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "График барбер на этот день отсутствует"
                ));
    }

    @Transactional(readOnly = true)
    public List<WorkShiftResponse> findByWorkDateBetween(UUID barberId, LocalDate dateFrom, LocalDate dateTo) {
        return workShiftRepository.findByBarberEntity_IdAndWorkDateBetween(barberId, dateFrom, dateTo)
                .stream()
                .map(workShiftMapper::toResponse)
                .toList();
    }
}