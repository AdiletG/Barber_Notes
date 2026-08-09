package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceEntity;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceMapper;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceRepository;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.ScheduleConflictException;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerRepository;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {
    private final CustomerRepository customerRepository;
    private final BarberService  barberService;
    private final BranchService branchService;
    private final ServiceService  serviceService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentServiceMapper  appointmentServiceMapper;
    private final AppointmentServiceRepository  appointmentServiceRepository;

    @Transactional
    public AppointmentResponse execute(AppointmentCreateRequest request) {
        Optional<CustomerEntity> customerEntity  = customerRepository
                .findByPhoneNumber(request.getCustomerPhoneNumber());

        CustomerEntity customer;
        if(customerEntity.isPresent()) {
            customer = customerEntity.get();

        }else {
            if(request.getCustomerName() == null || request.getCustomerName().isEmpty()) {
                throw new BusinessRuleViolationException(
                        ErrorCode.CUSTOMER_NAME_NOT_FOUND,
                        "Имя пользователя не может быть пустым"
                );
            }

            customer = new CustomerEntity();
            customer.setFirstName(request.getCustomerName());
            customer.setPhoneNumber(request.getCustomerPhoneNumber());
            customer.setCreatedFrom(request.getSource());
            customer.setStatus(Status.ACTIVE);
            customerRepository.save(customer);
        }

        BarberEntity barberEntity = barberService.getById(request.getBarberId());
        BranchEntity branchEntity = branchService.getById(request.getBranchId());

        if(!barberEntity.getBranchEntity().getId().equals(branchEntity.getId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Барбер должен работать в выбранном филиале "
            );
        }

        List<ServiceEntity> serviceEntities = new ArrayList<>();
        Integer totalDurationMinutes = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;


        for (UUID serviceId : request.getServiceIds()) {
            ServiceEntity serviceEntity = serviceService.getById(serviceId);

            if(serviceEntity.getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Статус сервиса НЕАКТИВЕН"
                );
            }

            if (serviceEntity.getDurationMinutes() != null) {
                totalDurationMinutes += serviceEntity.getDurationMinutes();
            }

            if (serviceEntity.getPrice() != null) {
                totalPrice = totalPrice.add(serviceEntity.getPrice());
            }
            serviceEntities.add(serviceEntity);
        }

        AppointmentEntity  appointmentEntity = new AppointmentEntity();
        appointmentEntity.setCustomerEntity(customer);
        appointmentEntity.setBarberEntity(barberEntity);
        appointmentEntity.setBranchEntity(branchEntity);
        appointmentEntity.setAppointmentDate(request.getAppointmentDate());
        appointmentEntity.setStartTime(request.getStartTime());
        appointmentEntity.setTotalPrice(totalPrice);
        appointmentEntity.setTotalDurationMinutes(totalDurationMinutes);
        appointmentEntity.setStatus(AppointmentStatus.CONFIRMED);
        appointmentEntity.setSource(request.getSource());

        try {
            appointmentRepository.save(appointmentEntity);
        }catch (DataIntegrityViolationException e) {
            throw new ScheduleConflictException(
                    ErrorCode.SCHEDULE_CONFLICT,
                    "Ошибка при создании записи"
            );
        }


        List<AppointmentServiceEntity> appointmentServiceEntities = new ArrayList<>();


        for(ServiceEntity serviceEntity : serviceEntities) {
            AppointmentServiceEntity appointmentService =  appointmentServiceMapper
                    .serviceToSnapshotEntity(serviceEntity);

            appointmentService.setAppointmentEntity(appointmentEntity);

            appointmentServiceEntities.add(appointmentService);
        }

        appointmentServiceRepository.saveAll(appointmentServiceEntities);

        return appointmentMapper.toResponse(appointmentEntity);
    }
}