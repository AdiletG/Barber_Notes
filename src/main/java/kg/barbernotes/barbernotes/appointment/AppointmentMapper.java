package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.appointment.dto.AppointmentResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AppointmentMapper {

    @Mapping(target = "customerId", source = "customerEntity.id")
    @Mapping(target = "barberId", source = "barberEntity.id")
    @Mapping(target = "branchId", source = "branchEntity.id")
    AppointmentResponse toResponse(AppointmentEntity entity);
}
