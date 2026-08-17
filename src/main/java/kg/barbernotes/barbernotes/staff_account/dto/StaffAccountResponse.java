package kg.barbernotes.barbernotes.staff_account.dto;

import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAccountResponse {
    private UUID id;
    private String phoneNumber;
    private StaffRole role;
    private UUID barberId;
    private UUID branchId;
    private Status status;
    private Boolean mustChangePassword;
    private OffsetDateTime lastLoginAt;
}