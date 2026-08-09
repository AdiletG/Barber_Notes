package kg.barbernotes.barbernotes.service;

import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.event.ServiceCategoryDeactivatedEvent;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ServiceCategoryService categoryService;

    @Transactional
    public ServiceResponse update(UUID id, ServiceUpdateRequest request){
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Сервис с таким id отсутствует"
                ));

        if(request.getCategoryId() != null && !categoryService.existsById(request.getCategoryId())){
            throw new EntityNotFoundException(
                    ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                    "Категория с таким id не существует"
            );
        }

        serviceMapper.updateEntityFromDto(request,service);
        serviceRepository.save(service);
        return serviceMapper.toResponse(service);
    }
    
    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {

        if(!categoryService.existsById(request.getCategoryId())){
            throw new EntityNotFoundException(
                    ErrorCode.SERVICE_CATEGORY_NOT_FOUND,
                    "Категория с таким id не существует"
            );
        }

        if(serviceRepository.existsByName(request.getName())){
            throw new BusinessRuleViolationException(
                    ErrorCode.NAME_ALREADY_EXISTS,
                    "Такой сервис уже существует"
            );
        }

        ServiceEntity service = serviceMapper.toEntity(request);
        service.setStatus(Status.ACTIVE);
        serviceRepository.save(service);
        return serviceMapper.toResponse(service);
    }

    @Transactional
    public ServiceResponse updateStatus(UUID id, StatusUpdateRequest request) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Сервис с таким id отсутствует"
                ));

        if(service.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE){
            service.setStatus(Status.INACTIVE);
            serviceRepository.save(service);
        } else if (service.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE) {
            service.setStatus(Status.INACTIVE);
            serviceRepository.save(service);
        }else if(service.getStatus() == Status.INACTIVE && request.getStatus() == Status.INACTIVE){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус сервиса уже НЕАКТИВНЫЙ"
            );
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус сервиса уже АКТИВНЫЙ"
            );
        }
        return serviceMapper.toResponse(service);
    }

    @Transactional
    @EventListener
    public void deactivateAllByCategoryId(ServiceCategoryDeactivatedEvent event) {
        List<ServiceEntity> services = serviceRepository
                .findByServiceCategoryEntity_IdAndStatus(event.getCategoryId(), Status.ACTIVE);
        
        services.forEach(service -> service.setStatus(Status.INACTIVE));
    }

    @Transactional(readOnly = true)
    public ServiceEntity getById(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Сервис с таким id отсутствует"
                ));
    }
    
    @Transactional(readOnly = true)
    public ServiceResponse findById(UUID id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Сервис с таким id отсутствует"
                ));
        
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> findCategoryAndStatus(UUID categoryId, Status status) {
        return serviceRepository.findByServiceCategoryEntity_IdAndStatus(categoryId,status)
                .stream()
                .map(serviceMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public Page<ServiceResponse> findPageCategoryAndStatus(UUID categoryId, Status status, Pageable pageable) {
        return serviceRepository.findByServiceCategoryEntity_IdAndStatus(categoryId,status, pageable)
                .map(serviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable)
                .map(serviceMapper::toResponse);
    }


    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAllByStatus(Status status, Pageable pageable) {
        return serviceRepository.findAllByStatus(status, pageable)
                .map(serviceMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAllByCategoryId(UUID categoryId, Pageable pageable) {
        return serviceRepository.findByServiceCategoryEntity_Id(categoryId, pageable)
                .map(serviceMapper::toResponse);
    }
}