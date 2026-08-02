package kg.barbernotes.barbernotes.appointment_service;

import kg.barbernotes.barbernotes.service.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AppointmentServiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointmentEntity", ignore = true)
    @Mapping(target = "serviceEntity", source = ".")
    @Mapping(target = "serviceName", source = "serviceEntity.name")
    AppointmentServiceEntity serviceToSnapshotEntity(ServiceEntity serviceEntity);

    @Mapping(target = "serviceId", source = "serviceEntity.id")
    AppointmentServiceResponse toResponse(AppointmentServiceEntity snapshotEntity);
}
