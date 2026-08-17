package kg.barbernotes.barbernotes.staff_account.dto;

import kg.barbernotes.barbernotes.common.enums.StaffRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAccountCreateResponse {
    private UUID id;
    private String phoneNumber;
    private String temporaryPassword;
    private StaffRole role;
}