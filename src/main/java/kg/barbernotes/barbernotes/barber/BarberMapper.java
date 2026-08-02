package kg.barbernotes.barbernotes.barber;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BarberMapper {

    BarberEntity toEntity(BarberCreateRequest request);

    @Mapping(target = "branchId", source = "branchEntity.id")
    BarberResponse toResponse(BarberEntity barber);

    void updateBarber(BarberUpdateRequest request, @MappingTarget BarberEntity barber);
}