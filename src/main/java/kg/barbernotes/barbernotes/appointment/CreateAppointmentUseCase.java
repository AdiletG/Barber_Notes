package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.appointment.dto.AppointmentCreateRequest;
import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceEntity;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceMapper;
import kg.barbernotes.barbernotes.appointment_service.AppointmentServiceRepository;
import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.config.BookingConfig;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {
    private final CustomerRepository customerRepository;
    private final BarberService  barberService;
    private final BranchService branchService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentServiceMapper  appointmentServiceMapper;
    private final AppointmentServiceRepository  appointmentServiceRepository;
    private final WorkShiftService  workShiftService;
    private final Clock clock;
    private final RateLimiterService  rateLimiterService;
    private final RateLimitProperties rateLimitProperties;
    private final BookingServiceResolver bookingServiceResolver;
    private final BookingConfig bookingConfig;

    @Transactional
    public AppointmentResponse execute(AppointmentCreateRequest request) {

        bookingServiceResolver.validateCount(request.getServiceIds());

        LocalDateTime appointmentDateTime = LocalDateTime.of(
                request.getAppointmentDate(),
                request.getStartTime()
        );

        LocalDateTime now = LocalDateTime.now(clock);

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

        BookingServiceResolver.ResolvedServices resolved =
                bookingServiceResolver.resolve(
                        barberEntity.getId(),
                        request.getServiceIds()
                );

        int totalDurationMinutes = resolved.totalDurationMinutes();
        BigDecimal totalPrice = resolved.totalPrice();
        List<ServiceEntity> serviceEntities = resolved.serviceEntities();

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


        LocalTime shiftStart = workShift.getStartTime();
        LocalTime appointmentStart = request.getStartTime();

        int slotStepMinutes = bookingConfig.slotStepMinutes();

        boolean hasSeconds =
                appointmentStart.getSecond() != 0
                        || appointmentStart.getNano() != 0;

        long minutesFromShiftStart =
                ChronoUnit.MINUTES.between(shiftStart, appointmentStart);

        if (hasSeconds
                || minutesFromShiftStart % slotStepMinutes != 0) {

            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Время начала записи должно соответствовать шагу сетки "
                            + slotStepMinutes + " минут"
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
            log.warn(
                    "Failed to save appointment. Database error: {}",
                    e.getMostSpecificCause().getMessage()
            );

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