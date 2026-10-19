package kg.barbernotes.barbernotes.service;

import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.service.dto.ServiceCreateRequest;
import kg.barbernotes.barbernotes.service.dto.ServiceResponse;
import kg.barbernotes.barbernotes.service.dto.ServiceUpdateRequest;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryEntity;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
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

        ServiceCategoryEntity category =
                categoryService.getById(request.getCategoryId());

        if(category.getStatus() != Status.ACTIVE){
            throw new BusinessRuleViolationException(
                    ErrorCode.CATEGORY_IS_INACTIVE,
                    "Категория услуги НЕАКТИВНАЯ"
            );
        }
        service.setServiceCategoryEntity(category);
        serviceMapper.updateEntityFromDto(request,service);
        serviceRepository.save(service);
        return serviceMapper.toResponse(service);
    }
    
    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {

        ServiceCategoryEntity category = categoryService.getById(request.getCategoryId());

        if(category.getStatus() != Status.ACTIVE){
            throw new BusinessRuleViolationException(
                    ErrorCode.CATEGORY_IS_INACTIVE,
                    "Категория НЕАКТИВНА, нельзя создать сервис"
            );
        }

        if(serviceRepository.existsByName(request.getName())){
            throw new BusinessRuleViolationException(
                    ErrorCode.NAME_ALREADY_EXISTS,
                    "Такой сервис уже существует"
            );
        }

        ServiceEntity service = serviceMapper.toEntity(request);
        service.setServiceCategoryEntity(category);
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
            ServiceCategoryEntity category =
                    categoryService.getById(service.getServiceCategoryEntity().getId());

            if(category.getStatus() != Status.ACTIVE){
                throw new BusinessRuleViolationException(
                        ErrorCode.CATEGORY_IS_INACTIVE,
                        "Категория услуги НЕАКТИВНАЯ, нельзя активировать услугу"
                );
            }

            service.setStatus(Status.ACTIVE);

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