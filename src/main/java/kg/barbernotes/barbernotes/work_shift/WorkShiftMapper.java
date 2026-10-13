package kg.barbernotes.barbernotes.work_shift;

import kg.barbernotes.barbernotes.work_shift.dto.WorkShiftRequest;
import kg.barbernotes.barbernotes.work_shift.dto.WorkShiftResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkShiftMapper {

    WorkShiftEntity toEntity(WorkShiftRequest request);

    @Mapping(target = "barberId", source = "barberEntity.id")
    WorkShiftResponse toResponse(WorkShiftEntity entity);

    void updateEntity(WorkShiftRequest request, @MappingTarget WorkShiftEntity entity);
}
