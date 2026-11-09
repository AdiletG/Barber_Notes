package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import kg.barbernotes.barbernotes.appointment.dto.AppointmentStatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final CurrentUserProvider  currentUserProvider;
    private final Clock clock;

    @Transactional
    public void cancelByCustomer(UUID customerId, UUID appointmentId){
        AppointmentEntity appointment = appointmentRepository
                .findOwnById(customerId, appointmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Запись не найдена"
                ));

        if(appointment.getStatus() != AppointmentStatus.CONFIRMED){
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Недопустимое изменение статуса"
            );
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getStartTime()
        );

        if (!appointmentDateTime.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Нельзя отменить запись, которая уже началась"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

    }

    @Transactional
    public AppointmentResponse statusUpdate(UUID id, AppointmentStatusUpdateRequest request) {
        AppointmentEntity appointment = getById(id);

        checkCurrentUser(appointment);

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getStartTime()
        );

        LocalDateTime now = LocalDateTime.now(clock);

        if((request.getStatus() == AppointmentStatus.COMPLETED || request.getStatus() == AppointmentStatus.NO_SHOW)
                && appointmentDateTime.isAfter(now) ){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Нельзя отметить запись завершённой или неявкой до её начала"
            );
        }

        if(appointment.getStatus() == AppointmentStatus.CONFIRMED &&
                (request.getStatus() == AppointmentStatus.COMPLETED ||
                request.getStatus() == AppointmentStatus.CANCELLED ||
                request.getStatus() == AppointmentStatus.NO_SHOW)) {
            appointment.setStatus(request.getStatus());
            appointmentRepository.save(appointment);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Недопустимое изменение статуса"
            );
        }

        return appointmentMapper.toResponse(appointment) ;
    }


    @Transactional(readOnly = true)
    public AppointmentResponse findOwnById(UUID customerId, UUID appointmentId) {

        AppointmentEntity appointment = appointmentRepository
                .findOwnById(customerId, appointmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.APPOINTMENT_NOT_FOUND,
                        "Запись не найдена"
                ));

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAllForCustomer(
            UUID customerId, AppointmentStatus status,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable
    ){
        return appointmentRepository.findAllWithFilters(
                status, null, null, customerId, dateFrom, dateTo, pageable
        ).map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean hasUpcomingBranch(UUID branchId,
                               AppointmentStatus status,
                               LocalDate fromDate,
                               LocalTime fromTime){
        return  appointmentRepository.hasUpcomingBranch(branchId, status, fromDate, fromTime);
    }

    @Transactional(readOnly = true)
    public boolean hasUpcoming(UUID barberId,
                               AppointmentStatus status,
                               LocalDate fromDate,
                               LocalTime fromTime){
        return  appointmentRepository.hasUpcoming(barberId, status, fromDate, fromTime);
    }

    @Transactional(readOnly = true)
    public Boolean existsById(UUID id) {
        return appointmentRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<AppointmentEntity> getConfirmedByBarberIdAndDate(UUID barberId, LocalDate date){
        return appointmentRepository.findByBarberEntity_IdAndAppointmentDateAndStatus(
                barberId, date, AppointmentStatus.CONFIRMED
        );
    }

    @Transactional(readOnly = true)
    public List<AppointmentEntity> getAppointmentByBarberIdAndDate(UUID barberId, LocalDate date) {
        return appointmentRepository
                .findAppointmentEntitiesByBarberEntity_IdAndAppointmentDate(barberId, date);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(UUID id) {
        AppointmentEntity appointment = getById(id);
        checkCurrentUser(appointment);
        return appointmentMapper.toResponse(appointment);
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
    public Page<AppointmentResponse> findALLFromFilters(
            AppointmentStatus status, UUID branchId, UUID barberId, UUID customerId,
            LocalDate dateFrom, LocalDate dateTo, Pageable pageable
    ){
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBarber()){
            barberId = currentUser.barberId();
        } else if (currentUser.isBranchAdmin()) {
            branchId = currentUser.branchId();
        }

        return appointmentRepository.findAllWithFilters(
                status, branchId, barberId, customerId, dateFrom, dateTo, pageable
        ).map(appointmentMapper::toResponse);
    }

    private void checkCurrentUser(AppointmentEntity appointment) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBarber() &&
                !currentUser.belongsToBarber(appointment.getBarberEntity().getId())){
            throw new AccessDeniedException("Барбер может просматривать только свои записи");
        } else if (currentUser.isBranchAdmin() &&
                !currentUser.belongsToBranch(appointment.getBranchEntity().getId())) {
            throw new AccessDeniedException("Администратор может просматривать только записи своего филиала");
        }
    }

}