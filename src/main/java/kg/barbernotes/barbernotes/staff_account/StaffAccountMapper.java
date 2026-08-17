package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StaffAccountMapper {
    StaffAccountEntity toEntity(StaffAccountCreateRequest request);
    StaffAccountResponse toResponse(StaffAccountEntity entity);
    void updateEntity(StaffAccountCreateRequest request, @MappingTarget StaffAccountEntity entity);
}
