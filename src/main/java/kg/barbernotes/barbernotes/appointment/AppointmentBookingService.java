package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        return appointmentRepository.findById(id)
                .map(appointmentMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Запись с таким id не существует"
                ));
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAllFromCustomerId(UUID customerId, Pageable pageable) {
        return appointmentRepository.findAllByCustomerEntity_Id(customerId, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAllFromBarberId(UUID barberId, Pageable pageable) {
        return appointmentRepository.findAllByBarberEntity_Id(barberId, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAllFromBranchId(UUID branchId, Pageable pageable) {
        return appointmentRepository.findAllByBranchEntity_Id(branchId, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findALLFromFilters(
            AppointmentStatus status, UUID branchId, UUID barberId, UUID customerId,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable
    ){
        return appointmentRepository.findAllWithFilters(
                status, branchId, barberId, customerId, dateFrom, dateTo, pageable
        ).map(appointmentMapper::toResponse);
    }

}