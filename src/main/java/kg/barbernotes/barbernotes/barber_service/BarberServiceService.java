package kg.barbernotes.barbernotes.barber_service;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.barber_service.dto.AddBarberServiceRequest;
import kg.barbernotes.barbernotes.barber_service.dto.BarberServiceResponse;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.event.BarberServiceDeactivatedEvent;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BarberServiceService {
    private final BarberServiceRepository barberServiceRepository;
    private final BarberServiceMapper barberServiceMapper;
    private final BarberService barberService;
    private final ServiceService serviceService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public BarberServiceResponse create(UUID barberId, AddBarberServiceRequest request){

        checkCurrentUser(barberId);

        BarberEntity barber = barberService.getById(barberId);
        ServiceEntity service = serviceService.getById(request.getServiceId());

        Optional<BarberServiceEntity> barberServiceEntity = barberServiceRepository
                .findByBarberEntity_IdAndServiceEntity_Id(barber.getId(), service.getId());

        BarberServiceEntity entityToSave = barberServiceEntity
                .map(existing -> {
                    existing.setStatus(Status.ACTIVE);
                    return existing;
                })
                .orElseGet(() -> {
                    BarberServiceEntity created = new BarberServiceEntity();
                    created.setBarberEntity(barber);
                    created.setServiceEntity(service);
                    created.setStatus(Status.ACTIVE);
                    return created;
                });

        barberServiceRepository.save(entityToSave);
        return barberServiceMapper.toResponse(entityToSave);
    }

    @Transactional
    public BarberServiceResponse updateStatus(UUID barberId, UUID serviceId, StatusUpdateRequest request) {

        checkCurrentUser(barberId);

        BarberServiceEntity serviceEntity = barberServiceRepository
                .findByBarberEntity_IdAndServiceEntity_Id(barberId, serviceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_SERVICE_NOT_FOUND,
                        "Такой сервис у барбера не существует"
                ));

        if(serviceEntity.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE) {
            serviceEntity.setStatus(Status.INACTIVE);
            barberServiceRepository.save(serviceEntity);
        }else if(serviceEntity.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE) {
            serviceEntity.setStatus(Status.ACTIVE);
            barberServiceRepository.save(serviceEntity);
        } else if (serviceEntity.getStatus() == Status.INACTIVE && request.getStatus() == Status.INACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус сервиса у барбера уже НЕАКТИВЕН"
            );
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус сервиса у барбера уже АКТИВЕН"
            );
        }

        return barberServiceMapper.toResponse(serviceEntity);
    }

    @Transactional
    @EventListener
    public void deactivatedBarberService(BarberServiceDeactivatedEvent event) {
        List<BarberServiceEntity> serviceServiceEntity = barberServiceRepository
                .findByBarberEntity_IdAndStatus(event.getBarberId(), Status.ACTIVE);
        serviceServiceEntity.forEach(service -> service.setStatus(Status.INACTIVE));
    }

    @Transactional(readOnly = true)
    public BarberServiceEntity getServiceByBarberId(UUID barberId,  UUID serviceId) {
        return barberServiceRepository.findByBarberEntity_IdAndServiceEntity_Id(barberId, serviceId)
                .orElseThrow( () -> new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Барбер не оказывает выбранную услугу"
                )
        );
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> getByBarberIdServiceId(UUID barberId, UUID serviceId) {
        return barberServiceRepository.findByBarberEntity_IdAndServiceEntity_Id(barberId, serviceId)
                .stream()
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> findByBarberId(UUID barberId) {
        return barberServiceRepository.findByBarberEntity_Id(barberId)
                .stream()
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> getByBarberStatus(UUID barberId, Status status) {
        return barberServiceRepository.findByBarberEntity_IdAndStatus(barberId, status)
                .stream()
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> findByBarberIdAndServiceIdAndStatus(UUID barberId, UUID serviceId, Status status) {
        return barberServiceRepository.findByBarberEntity_IdAndServiceEntity_IdAndStatus(
                barberId, serviceId, status).stream()
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    private void checkCurrentUser(UUID barberID) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        BarberEntity barber = barberService.getById(barberID);

        if(currentUser.isBranchAdmin()){
            if(!currentUser.belongsToBranch(barber.getBranchEntity().getId())){
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Сервисы барбера могут изменять только Администраторы филиала к которому привязан барбер"
                );
            }
        }
    }
}