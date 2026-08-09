package kg.barbernotes.barbernotes.barber;

import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.event.BarberServiceDeactivatedEvent;
import kg.barbernotes.barbernotes.common.event.BranchDeactivatedEvent;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberService {
    private final BarberRepository barberRepository;
    private final BarberMapper barberMapper;
    private final BranchService branchService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void update(UUID id, BarberUpdateRequest request) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбер с таким id отсутствует"
                ));

        if(request.getBranchId() != null && branchService.existsById(request.getBranchId())){
            throw new EntityNotFoundException(
                    ErrorCode.BRANCH_NOT_FOUND,
                    "Филиал с таким id отсутствуют"
            );
        }

        barberMapper.updateBarber(request,  barber);

        barberRepository.save(barber);
    }

    @Transactional
    public void create(BarberCreateRequest request){


        if(barberRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Барбер с таким номером уже существует"
            );
        }

        if(branchService.existsById(request.getBranchId())){
            throw new EntityNotFoundException(
                    ErrorCode.BRANCH_NOT_FOUND,
                    "Филиал с таким id отсутствуют"
            );
        }

        BarberEntity barber = barberMapper.toEntity(request);

        barberRepository.save(barber);
    }

    @Transactional
    @EventListener
    public void deactivateAllByBranchId(BranchDeactivatedEvent event) {
        List<BarberEntity> barbers = barberRepository
                .findByBranchEntity_IdAndStatus(event.getBranchId(),  Status.ACTIVE);
        barbers.forEach(barber -> barber.setStatus(Status.INACTIVE));
    }

    @Transactional
    public void inactive(UUID id) {
        BarberEntity barber = barberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_NOT_FOUND,
                        "Барбер с таким id отсутствует"
                ));

        if(barber.getStatus() == Status.INACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус барбера уже НЕАКТИВНЫЙ"
            );
        }

        barber.setStatus(Status.INACTIVE);
        barberRepository.save(barber);

        eventPublisher.publishEvent(new BarberServiceDeactivatedEvent(id));
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
    public List<BarberResponse> findByBranchIdAndStatusTOList(UUID branchId, Status status) {
        return barberRepository.findByBranchEntity_IdAndStatus(branchId, status)
                .stream()
                .map(barberMapper::toResponse)
                .toList();
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
}