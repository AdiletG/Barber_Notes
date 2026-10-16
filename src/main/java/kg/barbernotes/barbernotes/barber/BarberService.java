package kg.barbernotes.barbernotes.barber;

import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.barber.dto.BarberCreateRequest;
import kg.barbernotes.barbernotes.barber.dto.BarberResponse;
import kg.barbernotes.barbernotes.barber.dto.BarberUpdateRequest;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.event.BranchDeactivatedEvent;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberService {
    private final BarberRepository barberRepository;
    private final BarberMapper barberMapper;
    private final BranchService branchService;
    private final CurrentUserProvider currentUserProvider;
    private final AppointmentBookingService appointmentBookingService;
    private final Clock clock;


    @Transactional
    public BarberResponse update(UUID id, BarberUpdateRequest request) {

        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбер с таким id отсутствует"
                ));

        String barberPhoneNumber = PhoneNormalizer.normalize(request.getPhoneNumber());

        if(barberRepository.existsByPhoneNumber(barberPhoneNumber)) {
            throw new BusinessRuleViolationException(
                    ErrorCode.PHONE_NUMBER_ALREADY_EXISTS,
                    "Барбер с таким номером телефона уже существует"
            );
        }

        checkCurrentUser(barber);
        barber.setPhoneNumber(barberPhoneNumber);
        barberMapper.updateBarber(request,  barber);
        barberRepository.save(barber);
        return barberMapper.toResponse(barber);
    }

    @Transactional
    public BarberResponse create(BarberCreateRequest request){
        BranchEntity branch = branchService.getById(request.getBranchId());
        BarberEntity barber = barberMapper.toEntity(request);
        barber.setBranchEntity(branch);
        checkCurrentUser(barber);

        String barberPhoneNumber = PhoneNormalizer.normalize(barber.getPhoneNumber());
        if(barberRepository.existsByPhoneNumber(
                barberPhoneNumber)) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Барбер с таким номером уже существует"
            );
        }

        barber.setPhoneNumber(barberPhoneNumber);
        barber.setStatus(Status.ACTIVE);

        barberRepository.save(barber);
        return barberMapper.toResponse(barber);
    }

    @Transactional
    @EventListener
    public void deactivateAllByBranchId(BranchDeactivatedEvent event) {
        List<BarberEntity> barbers = barberRepository
                .findByBranchEntity_IdAndStatus(event.getBranchId(),  Status.ACTIVE);
        for(BarberEntity barber : barbers) {
            barber.setStatus(Status.INACTIVE);
            barberRepository.save(barber);
        }
    }

    @Transactional
    public BarberResponse updateStatus(UUID id, StatusUpdateRequest request) {

        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбер с таким id отсутствует"
                ));

        checkCurrentUser(barber);

        if(barber.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE) {

            if(appointmentBookingService.hasUpcoming(
                    barber.getId(), AppointmentStatus.CONFIRMED,
                    LocalDate.now(clock), LocalTime.now(clock))
            ){
                throw new BusinessRuleViolationException(
                        ErrorCode.BARBER_HAS_FUTURE_APPOINTMENTS,
                        "Барбера нельзя ДЕАКТИВИРОВАТЬ так как есть сформированные записи"
                );
            }

            barber.setStatus(Status.INACTIVE);
            barberRepository.save(barber);
        } else if (barber.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE) {
            barber.setStatus(Status.ACTIVE);
            barberRepository.save(barber);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Барбер уже имеет запрошенный статус"
            );
        }

        return barberMapper.toResponse(barber);
    }


    @Transactional(readOnly = true)
    public BarberEntity getById(UUID id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбера с таким id не существует"
                ));
    }

    @Transactional(readOnly = true)
    public BarberResponse findById(UUID id) {
        return barberRepository.findById(id)
                .map(barberMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбера с таким id не существует"
                ));
    }

    @Transactional(readOnly = true)
    public Page<BarberResponse> findByBranchIdAndStatus(UUID branchId, Status status, Pageable pageable) {
        return barberRepository.findByBranchEntity_IdAndStatus(branchId, status, pageable)
                .map(barberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BarberResponse> findByBranchId(UUID branchId, Pageable pageable) {
        return barberRepository.findByBranchEntity_Id(branchId, pageable)
                .map(barberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BarberResponse> findAllByStatus(Status status, Pageable pageable) {
        return barberRepository.findAllByStatus(status, pageable)
                .map(barberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BarberResponse> findAll(Pageable pageable) {
        return barberRepository.findAll(pageable)
                .map(barberMapper::toResponse);
    }

    private void checkCurrentUser(BarberEntity barber) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBranchAdmin() &&
                !currentUser.belongsToBranch(barber.getBranchEntity().getId())
        ) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Данные барбере могут изменять только Администраторы филиала к которому привязан барбер"
            );
        }
    }
}