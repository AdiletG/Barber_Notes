package kg.barbernotes.barbernotes.service_category;

import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.event.ServiceCategoryDeactivatedEvent;
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
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ServiceCategoryResponse update(UUID id, ServiceCategoryUpdateRequest request) {
        ServiceCategoryEntity category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                        "Категория с таким id отсутствует"
                ));

        serviceCategoryMapper.updateEntityFromDto(request, category);
        serviceCategoryRepository.save(category);
        return serviceCategoryMapper.toResponse(category);
    }

    @Transactional
    public ServiceCategoryResponse create(ServiceCategoryCreateRequest request) {
        ServiceCategoryEntity category = serviceCategoryMapper.toEntity(request);

        if(serviceCategoryRepository.existsByName((category.getName()))){
            throw new BusinessRuleViolationException(
                    ErrorCode.NAME_ALREADY_EXISTS,
                    "Категория с таким " + category.getName() + " именем уже существует"
            );
        }

        category.setStatus(Status.ACTIVE);
        serviceCategoryRepository.save(category);
        return serviceCategoryMapper.toResponse(category);
    }

    @Transactional
    public ServiceCategoryResponse inactive(UUID id, StatusUpdateRequest request) {
        ServiceCategoryEntity category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                        "Категория с таким id отсутствует"
                ));

        if(category.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE) {
            category.setStatus(Status.INACTIVE);
            serviceCategoryRepository.save(category);

            eventPublisher.publishEvent(new ServiceCategoryDeactivatedEvent(id));
        } else if (category.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE) {
            category.setStatus(Status.ACTIVE);
            serviceCategoryRepository.save(category);
        } else if (category.getStatus() == Status.INACTIVE && request.getStatus() == Status.INACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус категория уже НЕАКТИВНЫЙ"
            );
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус категория уже АКТИВНЫЙ"
            );
        }
        return serviceCategoryMapper.toResponse(category);
    }


    @Transactional(readOnly = true)
    public Page<ServiceCategoryResponse> findAllByStatus(Status status, Pageable pageable) {
        return serviceCategoryRepository.findAllByStatus(status, pageable)
                .map(serviceCategoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceCategoryResponse findByName(String name){
        ServiceCategoryEntity category = serviceCategoryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                        "Категория с таким (" + name + ") именем отсутствует"
                ));

        return serviceCategoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public boolean existsById(UUID id){
        return serviceCategoryRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public ServiceCategoryResponse findById(UUID id) {
        ServiceCategoryEntity category = serviceCategoryRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                "Категория отсутствует"
        ));
        return serviceCategoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public Page<ServiceCategoryResponse> getALLCategories(Pageable pageable) {
        return serviceCategoryRepository.findAll(pageable)
                .map(serviceCategoryMapper::toResponse);
    }
}