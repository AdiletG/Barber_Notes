package kg.barbernotes.barbernotes.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE

)
public interface ServiceMapper {

    ServiceEntity toEntity(ServiceCreateRequest request);

    @Mapping(target = "categoryId", source = "serviceCategoryEntity.id")
    ServiceResponse toResponse(ServiceEntity entity);

    void updateEntityFromDto(ServiceUpdateRequest request, @MappingTarget ServiceEntity entity);
}
