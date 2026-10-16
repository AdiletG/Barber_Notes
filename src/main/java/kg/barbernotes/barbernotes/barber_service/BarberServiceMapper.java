package kg.barbernotes.barbernotes.barber_service;

import kg.barbernotes.barbernotes.barber_service.dto.BarberServiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarberServiceMapper {

    @Mapping(target = "barberId", source = "barberEntity.id")
    @Mapping(target = "serviceId", source = "serviceEntity.id")
    BarberServiceResponse toResponse(BarberServiceEntity entity);
}
