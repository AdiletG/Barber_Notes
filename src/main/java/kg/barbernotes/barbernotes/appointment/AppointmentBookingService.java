package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
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

    @Transactional
    public AppointmentResponse statusUpdate(UUID id, AppointmentStatusUpdateRequest request) {
        AppointmentEntity appointment = getById(id);

        if(appointment.getStatus() == AppointmentStatus.CONFIRMED &&
                (request.getStatus() == AppointmentStatus.COMPLETED ||
                request.getStatus() == AppointmentStatus.CANCELLED ||
                request.getStatus() == AppointmentStatus.NO_SHOW)) {
            appointment.setStatus(request.getStatus());
            appointmentRepository.save(appointment);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Не допустимое изменение статуса"
            );
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Boolean existsById(UUID id) {
        return appointmentRepository.existsById(id);
    }

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
    public AppointmentEntity getById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Запись с таким id не существует"
                ));
    }

    @Transactional(readOnly = true)
    public Boolean existsByBarberEntity_IdAndAppointmentDateAndStatus(
            UUID barberEntity, LocalDate date, AppointmentStatus status) {
        return appointmentRepository.existsByBarberEntity_IdAndAppointmentDateAndStatus(
                barberEntity, date, status
        );
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

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findALLStatus(AppointmentStatus status, Pageable pageable){
        return appointmentRepository.findAllByStatus(status, pageable)
                .map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(Pageable pageable){
        return appointmentRepository.findAll(pageable).map(appointmentMapper::toResponse);
    }

}