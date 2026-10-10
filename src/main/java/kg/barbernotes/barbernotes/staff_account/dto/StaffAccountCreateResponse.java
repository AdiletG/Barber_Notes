package kg.barbernotes.barbernotes.staff_account.dto;

import kg.barbernotes.barbernotes.common.enums.StaffRole;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAccountCreateResponse {
    private UUID id;
    private String phoneNumber;

    @ToString.Exclude
    private String temporaryPassword;
    private StaffRole role;
}