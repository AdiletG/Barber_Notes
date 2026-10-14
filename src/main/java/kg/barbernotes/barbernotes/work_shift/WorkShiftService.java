package kg.barbernotes.barbernotes.work_shift;

import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentEntity;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.barber.*;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import kg.barbernotes.barbernotes.work_shift.dto.AvailableResponse;
import kg.barbernotes.barbernotes.work_shift.dto.WorkShiftRequest;
import kg.barbernotes.barbernotes.work_shift.dto.WorkShiftResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final CurrentUserProvider currentUserProvider;


    @Transactional(readOnly = true)
    public List<AvailableResponse> availableSlot(UUID barberId, LocalDate date, int durationMinutes) {
        WorkShiftEntity workShift = getByBarberIdWorkDate(barberId, date);
        List<AppointmentEntity> appointments =
                appointmentBookingService.getAppointmentByBarberIdAndDate(barberId, date);

        final int STEP_MINUTES = 15;

        List<AvailableResponse> slots = new ArrayList<>();

        LocalTime slotStart = workShift.getStartTime();
        LocalTime wsEndTime = workShift.getEndTime();

        while (!slotStart.plusMinutes(durationMinutes).isAfter(wsEndTime)) {
            LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);
            final LocalTime start = slotStart;

            boolean busy = appointments.stream().anyMatch(a -> {
                LocalTime aStartTime = a.getStartTime();
                LocalTime aEndTime = aStartTime.plusMinutes(a.getTotalDurationMinutes());

                return start.isBefore(aEndTime) && aStartTime.isBefore(slotEnd);
            });

            slots.add(new AvailableResponse(date, slotStart, slotEnd, !busy));

            slotStart = slotStart.plusMinutes(STEP_MINUTES);

        }


        return slots;
    }

    @Transactional
    public WorkShiftResponse update(UUID workShiftId, WorkShiftRequest request) {
        WorkShiftEntity workShift = workShiftRepository.findById(workShiftId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "Графика на эту дату не существует"
                ));

        BarberEntity barber = barberService.getById(workShift.getBarberEntity().getId());
        BranchEntity branch = branchService.getById(barber.getBranchEntity().getId());

        checkCurrentUser(branch);

        checkWorkShift(branch, request);

        workShiftMapper.updateEntity(request, workShift);

        try {
            workShiftRepository.save(workShift);

        }catch (DataIntegrityViolationException e){
            throw new BusinessRuleViolationException(
                    ErrorCode.SHIFT_ALREADY_EXISTS,
                    "Не удалось обновить график"
            );
        }
        return workShiftMapper.toResponse(workShift);
    }

    @Transactional
    public WorkShiftResponse create(UUID barberId, WorkShiftRequest request){
        BarberEntity barber = barberService.getById(barberId);
        BranchEntity branch = branchService.getById(barber.getBranchEntity().getId());

        checkCurrentUser(branch);

        checkWorkShift(branch,  request);



        WorkShiftEntity workShift = workShiftMapper.toEntity(request);
        if(workShift.getStartTime().isBefore(workShift.getStartTime())) {
            throw new BusinessRuleViolationException(
              ErrorCode.BUSINESS_RULE_VIOLATION,
              "Время начало смены должно быть до его окончания"
            );
        }

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

        BarberEntity barber = barberService.getById(workShift.getBarberEntity().getId());
        BranchEntity branch = branchService.getById(barber.getBranchEntity().getId());

        checkCurrentUser(branch);

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
    public WorkShiftEntity getByBarberIdWorkDate(UUID barberId, LocalDate date) {
        return workShiftRepository.findByBarberEntity_IdAndWorkDate(barberId, date)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.WORK_SHIFT_NOT_FOUND,
                        "График барбер на этот день отсутствует"
                ));
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

        if (dateFrom == null || dateTo == null) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Параметры dateFrom и dateTo должны передаваться вместе"
            );
        }

        return workShiftRepository.findByBarberEntity_IdAndWorkDateBetween(barberId, dateFrom, dateTo)
                .stream()
                .map(workShiftMapper::toResponse)
                .toList();
    }

    private void checkWorkShift(BranchEntity branch, WorkShiftRequest request){

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
    }

    private void checkCurrentUser(BranchEntity branch) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBranchAdmin()){
            if(!currentUser.belongsToBranch(branch.getId())){
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Данные барбере могут изменять только Администраторы филиала к которому привязан барбер"
                );
            }
        }
    }

}