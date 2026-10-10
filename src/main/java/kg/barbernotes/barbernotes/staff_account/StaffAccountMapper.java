
package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StaffAccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "mustChangePassword", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "barberEntity", ignore = true)
    @Mapping(target = "branchEntity", ignore = true)
    StaffAccountEntity toEntity(StaffAccountCreateRequest request);

    @Mapping(source = "barberEntity.id", target = "barberId")
    @Mapping(source = "branchEntity.id", target = "branchId")
    StaffAccountResponse toResponse(StaffAccountEntity entity);
}