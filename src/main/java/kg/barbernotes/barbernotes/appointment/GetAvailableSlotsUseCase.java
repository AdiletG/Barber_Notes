package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.common.config.BookingConfig;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.work_shift.WorkShiftEntity;
import kg.barbernotes.barbernotes.work_shift.WorkShiftService;
import kg.barbernotes.barbernotes.work_shift.dto.AvailableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAvailableSlotsUseCase {
    private final BarberService  barberService;
    private final BookingServiceResolver bookingServiceResolver;
    private final WorkShiftService workShiftService;
    private final AppointmentBookingService  appointmentBookingService;
    private final BookingConfig  bookingConfig;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<AvailableResponse> execute(
            UUID barberId,
            LocalDate date,
            List<UUID> serviceIds
    ) {

        bookingServiceResolver.validateCount(serviceIds);

        LocalDate today = LocalDate.now(clock);

        if (date.isBefore(today)) {
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_INPUT,
                    "Дата записи не может быть в прошлом"
            );
        }

        BarberEntity barberEntity = barberService.getById(barberId);

        if (barberEntity.getStatus() != Status.ACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус барбера НЕАКТИВЕН"
            );
        }

        if (barberEntity.getBranchEntity().getStatus() != Status.ACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус филиала НЕАКТИВЕН"
            );
        }

        BookingServiceResolver.ResolvedServices resolved =
                bookingServiceResolver.resolve(barberId, serviceIds);

        int totalDurationMinutes = resolved.totalDurationMinutes();

        List<WorkShiftEntity> workShifts =
                workShiftService.findByBarberIdWorkDate(barberId, date).stream().toList();

        if (workShifts.isEmpty()) {
            return List.of();
        }

        List<AppointmentEntity> appointments =
                appointmentBookingService
                        .getConfirmedByBarberIdAndDate(barberId, date);

        List<AvailableResponse> slots = new ArrayList<>();

        int slotStepMinutes = bookingConfig.slotStepMinutes();
        LocalTime currentTime = LocalTime.now(clock);

        for (WorkShiftEntity shift : workShifts) {

            int shiftStartMinutes =
                    shift.getStartTime().getHour() * 60
                            + shift.getStartTime().getMinute();

            int shiftEndMinutes =
                    shift.getEndTime().getHour() * 60
                            + shift.getEndTime().getMinute();

            int slotStartMinutes = shiftStartMinutes;

            while (slotStartMinutes + totalDurationMinutes <= shiftEndMinutes) {

                int slotEndMinutes =
                        slotStartMinutes + totalDurationMinutes;

                boolean isOccupied = false;

                for (AppointmentEntity appointment : appointments) {

                    int appointmentStartMinutes =
                            appointment.getStartTime().getHour() * 60
                                    + appointment.getStartTime().getMinute();

                    int appointmentEndMinutes =
                            appointmentStartMinutes
                                    + appointment.getTotalDurationMinutes();

                    boolean overlaps =
                            slotStartMinutes < appointmentEndMinutes
                                    && appointmentStartMinutes < slotEndMinutes;

                    if (overlaps) {
                        isOccupied = true;
                        break;
                    }
                }

                boolean isInPast = date.equals(today)
                        && LocalTime.of(
                        slotStartMinutes / 60,
                        slotStartMinutes % 60
                ).isBefore(currentTime);

                boolean isAvailable = !isOccupied && !isInPast;

                LocalTime startSlotTime = LocalTime.of(
                        slotStartMinutes / 60,
                        slotStartMinutes % 60
                );

                LocalTime endSlotTime = LocalTime.of(
                        slotEndMinutes / 60,
                        slotEndMinutes % 60
                );

                slots.add(new AvailableResponse(
                        date,
                        startSlotTime,
                        endSlotTime,
                        isAvailable
                ));

                slotStartMinutes += slotStepMinutes;
            }
        }

        return slots;
    }
}