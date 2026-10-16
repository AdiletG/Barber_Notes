package kg.barbernotes.barbernotes.barber;

import kg.barbernotes.barbernotes.barber.dto.BarberCreateRequest;
import kg.barbernotes.barbernotes.barber.dto.BarberResponse;
import kg.barbernotes.barbernotes.barber.dto.BarberUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarberMapper {

    @Mapping(target = "branchEntity", ignore = true)
    @Mapping(target = "status", ignore = true)
    BarberEntity toEntity(BarberCreateRequest request);

    @Mapping(target = "branchId", source = "branchEntity.id")
    BarberResponse toResponse(BarberEntity barber);

    void updateBarber(BarberUpdateRequest request, @MappingTarget BarberEntity barber);
}