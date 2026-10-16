package kg.barbernotes.barbernotes.branch;

import kg.barbernotes.barbernotes.appointment.AppointmentBookingService;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import kg.barbernotes.barbernotes.branch.dto.BranchCreateRequest;
import kg.barbernotes.barbernotes.branch.dto.BranchResponse;
import kg.barbernotes.barbernotes.branch.dto.BranchUpdateRequest;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.event.BranchDeactivatedEvent;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final AppointmentBookingService appointmentBookingService;
    private final Clock clock;


    @Transactional
    public BranchResponse update(UUID id, BranchUpdateRequest request){
        BranchEntity branch = getById(id);
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBranchAdmin() &&
                !currentUser.belongsToBranch(branch.getId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Администратор филиала может изменять данные только своего филиала"
            );
        }

        branchMapper.updateEntityFromDto(request, branch);

        if(!branch.getOpenTime().isBefore(branch.getCloseTime())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала работы должно быть меньше времени конца"
            );
        }

        branchRepository.save(branch);
        return branchMapper.toResponse(branch);
    }

    @Transactional
    public BranchResponse create(BranchCreateRequest request) {
        BranchEntity branch =  branchMapper.toEntity(request);

        if(!branch.getOpenTime().isBefore(branch.getCloseTime())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала работы должно быть меньше времени конца"
                    );
        }

        branch.setStatus(Status.ACTIVE);
        branchRepository.save(branch);
        return branchMapper.toResponse(branch);
    }

    @Transactional
    public BranchResponse inactive(UUID id, StatusUpdateRequest request) {
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BRANCH_NOT_FOUND,
                        "Филиал с таким id отсутствует"
                ));

        if(branch.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE){

            if(appointmentBookingService.hasUpcomingBranch(
                    branch.getId(), AppointmentStatus.CONFIRMED,
                    LocalDate.now(clock), LocalTime.now(clock))
            ){
                throw new BusinessRuleViolationException(
                        ErrorCode.BRANCH_HAS_FUTURE_APPOINTMENTS,
                        "Филиал нельзя ДЕАКТИВИРОВАТЬ так как есть сформированные записи"
                );
            }

            branch.setStatus(request.getStatus());
            branchRepository.save(branch);
            eventPublisher.publishEvent(new BranchDeactivatedEvent(id));

        }else if(branch.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE){
            branch.setStatus(request.getStatus());
            branchRepository.save(branch);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус филиала уже АКТИВНЫЙ"
            );
        }

        return branchMapper.toResponse(branch);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> findAllByStatus(Status status, Pageable pageable) {
        return  branchRepository.findAllByStatus(status, pageable)
                .map(branchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BranchEntity getById(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BRANCH_NOT_FOUND,
                        "Филиал с таким id отсутствуют"
                ));
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(UUID id) {
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BRANCH_NOT_FOUND,
                        "Филиал с таким id отсутствуют"
                ));

        return branchMapper.toResponse(branch);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> getAllBranches(Pageable pageable) {
        return branchRepository.findAll(pageable).map(branchMapper::toResponse);
    }
}