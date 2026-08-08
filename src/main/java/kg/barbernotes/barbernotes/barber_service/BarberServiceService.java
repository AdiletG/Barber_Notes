package kg.barbernotes.barbernotes.barber_service;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.event.BarberServiceDeactivatedEvent;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    @Transactional
    public void create(UUID barberId, AddBarberServiceRequest request){
        BarberEntity barber = barberService.getById(barberId);
        ServiceEntity service = serviceService.getById(request.getServiceId());

        Optional<BarberServiceEntity> barberServiceEntity = barberServiceRepository
                .findByBarberEntity_IdAndServiceEntity_Id(barber.getId(), service.getId());

        BarberServiceEntity entityToSave;

        if(barberServiceEntity.isPresent()) {
            BarberServiceEntity existing =  barberServiceEntity.get();

                if(existing.getStatus() == Status.INACTIVE) {
                    existing.setStatus(Status.ACTIVE);
                    entityToSave = existing;
                }else {
                    entityToSave = existing;
                }
        }else {
            entityToSave = new BarberServiceEntity();
            entityToSave.setBarberEntity(barber);
            entityToSave.setServiceEntity(service);
            entityToSave.setStatus(Status.ACTIVE);
        }

        barberServiceRepository.save(entityToSave);
    }

    @Transactional
    public void inactive(UUID barberId, UUID serviceId) {
        BarberServiceEntity serviceEntity = barberServiceRepository
                .findByBarberEntity_IdAndServiceEntity_Id(barberId, serviceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Такой сервис у барбера не существует"
                ));

        if(serviceEntity.getStatus() == Status.ACTIVE) {
            serviceEntity.setStatus(Status.INACTIVE);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус сервиса у барбера уже НЕАКТИВЕН"
            );
        }

        barberServiceRepository.save(serviceEntity);
    }


    @Transactional
    @EventListener
    public void deactivatedBarberService(BarberServiceDeactivatedEvent event) {
        List<BarberServiceEntity> serviceService = barberServiceRepository
                .findByBarberEntity_IdAndStatus(event.getBarberId(), Status.ACTIVE);
        serviceService.forEach(service -> service.setStatus(Status.INACTIVE));
    }

    @Transactional(readOnly = true)
    public List<BarberServiceResponse> getByBarberIdStatusList(UUID barberId, Status status) {
        return barberServiceRepository.findByBarberEntity_IdAndStatus(barberId, status)
                .stream()
                .map(barberServiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<BarberServiceResponse> getByBarberId(UUID barberId, Pageable pageable) {
        return barberServiceRepository.findByBarberEntity_Id(barberId, pageable)
                .map(barberServiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BarberServiceResponse> getByBarberStatusPage(UUID barberId, Status status, Pageable pageable) {
        return barberServiceRepository.findByBarberEntity_IdAndStatus(barberId, status, pageable)
                .map(barberServiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BarberServiceResponse findByBarberAndService(UUID barberId, UUID serviceId) {
        return barberServiceRepository.findByBarberEntity_IdAndServiceEntity_Id(barberId, serviceId)
                .map(barberServiceMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.BARBER_SERVICE_NOT_FOUND,
                        "Данный сервис отсутствует у барбера"
                ));
    }
}