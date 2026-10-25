package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.barber_service.BarberServiceEntity;
import kg.barbernotes.barbernotes.barber_service.BarberServiceService;
import kg.barbernotes.barbernotes.common.config.BookingConfig;
import kg.barbernotes.barbernotes.common.config.RateLimitProperties;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.service.ServiceEntity;
import kg.barbernotes.barbernotes.service.ServiceService;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryEntity;
import kg.barbernotes.barbernotes.service_category.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingServiceResolver {
    private final ServiceService serviceService;
    private final ServiceCategoryService categoryService;
    private final BarberServiceService barberServiceService;
    private final RateLimitProperties rateLimitProperties;
    private final BookingConfig bookingConfig;

    public record ResolvedServices(
            List<ServiceEntity> serviceEntities,
            int totalDurationMinutes,
            BigDecimal totalPrice

    ){}

    public void validateCount(List<UUID> serviceIds) {
        int maxServices =
                rateLimitProperties.booking().maxServicesPerRecord();

        if (serviceIds == null
                || serviceIds.isEmpty()
                || serviceIds.size() > maxServices) {

            throw new BusinessRuleViolationException(
                    ErrorCode.INVALID_INPUT,
                    "На одну запись можно выбрать от 1 до "
                            + maxServices + " услуг"
            );
        }
    }

    public ResolvedServices resolve(UUID barberId, List<UUID> serviceIds){

        validateCount(serviceIds);

        List<ServiceEntity> serviceEntities = new ArrayList<>();
        int totalDurationMinutes = bookingConfig.breakMinutes();
        BigDecimal totalPrice = BigDecimal.ZERO;

        Set<UUID> seen = new HashSet<>();

        for (UUID serviceId : serviceIds) {

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
                    barberServiceService.getServiceByBarberId(barberId, serviceId);

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

        return new ResolvedServices(serviceEntities, totalDurationMinutes, totalPrice);
    }
}