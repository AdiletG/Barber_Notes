package kg.barbernotes.barbernotes.branch;

import kg.barbernotes.barbernotes.common.event.BranchDeactivatedEvent;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void update(UUID id, BranchUpdateRequest request){
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BRANCH_NOT_FOUND));

        branchMapper.updateEntityFromDto(request, branch);

        if(!branch.getOpenTime().isBefore(branch.getCloseTime())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала работы должно быть меньше времени конца"
            );
        }

        branchRepository.save(branch);
    }

    @Transactional
    public void create(BranchCreateRequest request) {
        BranchEntity branch =  branchMapper.toEntity(request);

        if(!branch.getOpenTime().isBefore(branch.getCloseTime())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала работы должно быть меньше времени конца"
                    );
        }

        branch.setStatus(Status.ACTIVE);
        branchRepository.save(branch);
    }

    @Transactional

    public void inactive(UUID id) {
        BranchEntity branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BRANCH_NOT_FOUND,
                        "Филиал с таким id отсутствует"
                ));

        if(branch.getStatus() == Status.INACTIVE){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус филиала уже НЕАКТИВНЫЙ"
            );
        }

        branch.setStatus(Status.INACTIVE);
        branchRepository.save(branch);

        eventPublisher.publishEvent(new BranchDeactivatedEvent(id));
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> findAllByStatus(Status status, Pageable pageable) {
        return  branchRepository.findAllByStatus(status, pageable)
                .map(branchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return branchRepository.existsById(id);
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
    public BranchResponse getByAddress(String address) {
        BranchEntity branch = branchRepository.findByAddress(address)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BRANCH_NOT_FOUND,
                        "Филиал с таким " + address + " отсутствуют"
                ));

        return branchMapper.toResponse(branch);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        List<BranchEntity> branch = branchRepository.findAll();
                return branch.stream()
                        .map(branchMapper::toResponse)
                        .toList();
    }
}