package kg.barbernotes.barbernotes.work_shift;

import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.barber.*;
import kg.barbernotes.barbernotes.branch.BranchResponse;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AppointmentBookingService appointmentBookingService;

    @Transactional
    public WorkShiftResponse update(UUID workShiftId, WorkShiftUpdateRequest request) {
        WorkShiftEntity workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "Графика на эту дату не существует"
                ));

        BarberEntity barber = barberService.getById(workShift.getBarberEntity().getId());
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

        workShiftMapper.updateEntity(request, workShift);
        workShiftRepository.save(workShift);
        return workShiftMapper.toResponse(workShift);
    }

    @Transactional
    public WorkShiftResponse create(UUID barberId, WorkShiftCreateRequest request){
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
        try {
            workShiftRepository.save(workShift);

        }catch (DataIntegrityViolationException e){
            throw new BusinessRuleViolationException(
                    ErrorCode.SHIFT_ALREADY_EXISTS,
                    "Не удалось создать график так как график уже существует"
            );
        }
        return workShiftMapper.toResponse(workShift);
    }

    @Transactional
    public void delete(UUID workShiftId) {
        WorkShiftEntity workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "Графика по данному id не существует"
                ));
        if(appointmentBookingService.existsByBarberEntity_IdAndAppointmentDateAndStatus(
            workShift.getBarberEntity().getId(), workShift.getWorkDate(), AppointmentStatus.CONFIRMED
        )){
            throw new BusinessRuleViolationException(
                    ErrorCode.SHIFT_HAS_APPOINTMENTS,
                    "График на эту дату нельзя удалить так как есть сформированная запись"
            );
        }

        workShiftRepository.delete(workShift);
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
    public List<WorkShiftResponse> findByWorkShift(UUID barberId) {
        return workShiftRepository.findByBarberEntity_Id(barberId)
                .stream()
                .map(workShiftMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkShiftResponse> findByWorkDateBetween(UUID barberId, LocalDate dateFrom, LocalDate dateTo) {
        return workShiftRepository.findByBarberEntity_IdAndWorkDateBetween(barberId, dateFrom, dateTo)
                .stream()
                .map(workShiftMapper::toResponse)
                .toList();
    }

}