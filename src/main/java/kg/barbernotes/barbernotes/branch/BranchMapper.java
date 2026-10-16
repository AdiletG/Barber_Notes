package kg.barbernotes.barbernotes.branch;

import kg.barbernotes.barbernotes.branch.dto.BranchCreateRequest;
import kg.barbernotes.barbernotes.branch.dto.BranchResponse;
import kg.barbernotes.barbernotes.branch.dto.BranchUpdateRequest;
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