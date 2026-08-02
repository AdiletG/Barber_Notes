package kg.barbernotes.barbernotes.branch;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BranchMapper {

    BranchEntity toEntity(BranchCreateRequest request);
    BranchResponse toResponse(BranchEntity entity);
    void updateEntityFromDto(BranchUpdateRequest request, @MappingTarget BranchEntity entity);
}