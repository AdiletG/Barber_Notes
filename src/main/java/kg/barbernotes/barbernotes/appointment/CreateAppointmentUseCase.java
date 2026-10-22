package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.appointment.dto.AppointmentCreateRequest;
import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceEntity;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceMapper;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceRepository;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.barber_service.BarberServiceEntity;
import kg.barbernotes.barbernotes.barber_service.BarberServiceService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.config.RateLimitProperties;
import kg.barbernotes.barbernotes.common.enums.RateLimitGroup;
import kg.barbernotes.barbernotes.common.exceptions.RateLimitException;
import kg.barbernotes.barbernotes.common.security.ratelimit.RateLimitDecision;
import kg.barbernotes.barbernotes.common.security.ratelimit.RateLimiterService;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Source;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.ScheduleConflictException;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerRepository;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service.ServiceService;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryEntity;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryService;
import kg.barbernotes.barbernotes.work_shift.WorkShiftEntity;
import kg.barbernotes.barbernotes.work_shift.WorkShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {
    private final CustomerRepository customerRepository;
    private final BarberService  barberService;
    private final BranchService branchService;
    private final ServiceService  serviceService;
    private final ServiceCategoryService  categoryService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentServiceMapper  appointmentServiceMapper;
    private final AppointmentServiceRepository  appointmentServiceRepository;
    private final WorkShiftService  workShiftService;
    private final BarberServiceService barberServiceService;
    private final Clock clock;
    private final RateLimiterService  rateLimiterService;
    private final RateLimitProperties rateLimitProperties;

    private static final int BREAK_MINUTES = 15;

    @Transactional
    public AppointmentResponse execute(AppointmentCreateRequest request) {

        int serviceLimit = rateLimitProperties.booking().maxServicesPerRecord();

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                request.getAppointmentDate(),
                request.getStartTime()
        );

        LocalDateTime now = LocalDateTime.now(clock);

        if(request.getServiceIds().size() > serviceLimit){
            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_INPUT,
                    "На одну запись максимум " + serviceLimit + " услуг"
            );
        }

        String customerPhoneNumber = PhoneNormalizer.normalize(request.getCustomerPhoneNumber());

        String number = PhoneNormalizer.mask(customerPhoneNumber);

        RateLimitDecision decision = rateLimiterService.check(
                RateLimitGroup.PHONE,
                customerPhoneNumber
        );

        if(!decision.allowed()){
            log.warn(
                    "Rate limit exceeded: client PHONE ={}, retryAfter ={}",
                    number, decision.retryAfterSeconds()
            );

            throw new RateLimitException(
                    decision.retryAfterSeconds()
            );
        }

        //Customer
        Optional<CustomerEntity> customerEntity  = customerRepository
                .findByPhoneNumber(customerPhoneNumber);

        CustomerEntity customer;
        if(customerEntity.isPresent()) {
            customer = customerEntity.get();

            if(customer.getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Статус клиента неактивен либо заблокирован "
                        );
            }

            long appointmentCount = appointmentRepository.countCustomerFutureAppointments(
                    customer.getId(), AppointmentStatus.CONFIRMED,
                    now.toLocalDate(), now.toLocalTime()
            );

            int customerAppointmentLimit = rateLimitProperties.booking().maxActiveRecords();

            if(appointmentCount >= customerAppointmentLimit) {
                throw new BusinessRuleViolationException(
                        ErrorCode.CUSTOMER_APPOINTMENT_LIMIT_EXCEEDED,
                        "У клиента не может быть больше "
                        + customerAppointmentLimit + " активных записей"
                );
            }

        }else {
            if(request.getCustomerName() == null || request.getCustomerName().isEmpty()) {
                throw new BusinessRuleViolationException(
                        ErrorCode.CUSTOMER_NAME_NOT_FOUND,
                        "Имя пользователя не может быть пустым"
                );
            }

            customer = new CustomerEntity();
            customer.setFirstName(request.getCustomerName());
            customer.setPhoneNumber(customerPhoneNumber);
            customer.setCreatedFrom(Source.ONLINE);
            customer.setStatus(Status.ACTIVE);
            customerRepository.save(customer);
        }

        //Branch
        BranchEntity branchEntity = branchService.getById(request.getBranchId());
        if(branchEntity.getStatus() != Status.ACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус филиала НЕАКТИВЕН"
            );
        }

        //Barber
        BarberEntity barberEntity = barberService.getById(request.getBarberId());

        if(barberEntity.getStatus() != Status.ACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус барбера НЕАКТИВЕН"
            );
        }

        if(!barberEntity.getBranchEntity().getId().equals(branchEntity.getId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Барбер должен работать в выбранном филиале "
            );
        }

        //Service

        List<ServiceEntity> serviceEntities = new ArrayList<>();

        int totalDurationMinutes = BREAK_MINUTES;
        BigDecimal totalPrice = BigDecimal.ZERO;

        Set<UUID> seen = new HashSet<>();

        for (UUID serviceId : request.getServiceIds()) {
            ServiceEntity serviceEntity = serviceService.getById(serviceId);

            ServiceCategoryEntity category =
                    categoryService.getById(serviceEntity.getServiceCategoryEntity().getId());

            if(category.getStatus() != Status.ACTIVE){
                throw new BusinessRuleViolationException(
                        ErrorCode.CATEGORY_IS_INACTIVE,
                        "Категория услуги НЕАКТИВНАЯ"
                );
            }

            if(!seen.add(serviceId)){
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Услуга уже выбрана"
                );
            }

            if(serviceEntity.getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Статус сервиса НЕАКТИВЕН"
                );
            }

            BarberServiceEntity barberServiceEntity =
                    barberServiceService.getServiceByBarberId(barberEntity.getId(), serviceId);

            if(barberServiceEntity.getStatus() != Status.ACTIVE ) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Статус сервиса у барбера НЕАКТИВЕН"
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

        //Work Shift
        WorkShiftEntity workShift =
                workShiftService.getByBarberIdWorkDate(barberEntity.getId(), request.getAppointmentDate());

        if(workShift == null) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "В эту дату мастер не работает"
            );
        }

        if (request.getStartTime().isBefore(workShift.getStartTime())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "В это время мастер не работает"
            );
        }

        LocalTime endTime = request.getStartTime().plusMinutes(totalDurationMinutes);

        if(endTime.isAfter(workShift.getEndTime())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "запись выходит за пределы рабочего графика"
            );
        }


        if(appointmentDateTime.isBefore(now)){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время записи не может быть в прошлом"
            );
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
        appointmentEntity.setSource(Source.ONLINE);

        try {
            appointmentRepository.saveAndFlush(appointmentEntity);
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