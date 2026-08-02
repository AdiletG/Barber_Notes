package kg.barbernotes.barbernotes.service_category;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ServiceCategoryMapper {

    ServiceCategoryEntity toEntity(ServiceCategoryCreateRequest request);
    ServiceCategoryResponse toResponse(ServiceCategoryEntity entity);
    void updateEntityFromDto(ServiceCategoryUpdateRequest request, @MappingTarget ServiceCategoryEntity entity);
}