package kg.barbernotes.barbernotes.service_category;

import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service_category.dto.ServiceCategoryCreateRequest;
import kg.barbernotes.barbernotes.service_category.dto.ServiceCategoryResponse;
import kg.barbernotes.barbernotes.service_category.dto.ServiceCategoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public ServiceCategoryResponse update(UUID id, ServiceCategoryUpdateRequest request) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(!currentUser.isSuperAdmin()){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Изменение категорий услуг доступно только Супер Админу"
            );
        }
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
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(!currentUser.isSuperAdmin()){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Создание категорий услуг доступно только Супер Админу"
            );
        }

        ServiceCategoryEntity category = serviceCategoryMapper.toEntity(request);

        if(serviceCategoryRepository.existsByName((category.getName()))){
            throw new BusinessRuleViolationException(
                    ErrorCode.NAME_ALREADY_EXISTS,
                    "Категория с названием '" + category.getName() + "' уже существует"
            );
        }

        category.setStatus(Status.ACTIVE);
        serviceCategoryRepository.save(category);
        return serviceCategoryMapper.toResponse(category);
    }

    @Transactional
    public ServiceCategoryResponse updateStatus(UUID id, StatusUpdateRequest request) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(!currentUser.isSuperAdmin()){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Изменение категорий услуг доступно только Супер Админу"
            );
        }

        ServiceCategoryEntity category = serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                        "Категория с таким id отсутствует"
                ));

        if(category.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE) {

            for(ServiceEntity service : category.getServiceEntities()){
                if(service.getStatus() == Status.ACTIVE){
                    throw new BusinessRuleViolationException(
                            ErrorCode.CATEGORY_HAS_ACTIVE_SERVICE,
                            "Нельзя ДЕАКТИВИРОВАТЬ категорию если есть активные услуги"
                    );
                }
            }

            category.setStatus(Status.INACTIVE);
            serviceCategoryRepository.save(category);
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
                    "Категория уже имеет запрошенный статус"
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
    public ServiceCategoryEntity getById(UUID id) {
        return serviceCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                        "Категория отсутствует"
                ));
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