package kg.barbernotes.barbernotes.work_shift;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkShiftMapper {

    WorkShiftEntity toEntity(WorkShiftCreateRequest request);

    @Mapping(target = "barberId", source = "barberEntity.id")
    WorkShiftResponse toResponse(WorkShiftEntity entity);

    void updateEntity(WorkShiftUpdateRequest request, @MappingTarget WorkShiftEntity entity);
}
